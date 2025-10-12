package com.backend.services;

import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Happening;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.domain.reactions.EngagementStats;
import com.backend.port.inbound.IncidentUseCase;
import com.backend.port.inbound.commands.CoordinatesCommand;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import com.backend.port.inbound.commands.RadiusCommand;
import com.backend.port.inbound.commands.UploadMediaCommand;
import com.backend.port.outbound.repo.IncidentRepository;
import com.backend.port.outbound.storage.ObjectStoragePort;
import com.backend.services.exceptions.ActorNotFoundException;
import com.backend.services.exceptions.DuplicateIncidentException;
import com.backend.services.exceptions.IncidentAlreadyConfirmedException;
import com.backend.services.exceptions.IncidentAlreadyDeniedException;
import com.backend.services.exceptions.IncidentNotExpiredException;
import com.backend.services.exceptions.IncidentNotFoundException;
import com.backend.services.exceptions.InvalidCoordinatesException;
import com.backend.services.exceptions.LocationNotFoundException;
import com.backend.services.exceptions.ValidationException;

import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for handling operations on Incident entities.
 * Delegates persistence to the IncidentRepository.
 * <p>
 * Registered as a Spring {@link Service} for business logic orchestration.
 */
@Service
@RequiredArgsConstructor
public class IncidentService implements IncidentUseCase {

  private final IncidentRepository incidentRepository;
  private final ObjectStoragePort objectStoragePort;
  private final LocationService locationService;

    /**
     * Retrieves all Incident entries within a given visibility range.
     *
     * @param radiusCommand the command containing center coordinates and radius in meters
     * @return list of matching {@code Incident} instances.
     * @throws InvalidCoordinatesException if coordinates or radius are invalid
     */
    @Override
    public List<Incident> findAllInGivenRange(RadiusCommand radiusCommand)
            throws InvalidCoordinatesException {

        final double userLatitude = radiusCommand.lat();
        final double userLongitude = radiusCommand.lon();
        final double radiusMeters = radiusCommand.radius();

        if (radiusMeters < 0 || radiusMeters > 50000) // Max 50km
            throw new InvalidCoordinatesException("Radius must be between 0 and 50000 meters");

        try {
            return incidentRepository.findAllInGivenRange(userLatitude, userLongitude, radiusMeters);
        } catch (Exception e) {
            throw new InvalidCoordinatesException("Failed to search incidents in given range", e);
        }
    }

    /**
     * Finds all incidents created by a specific actor.
     *
     * @param actorId identifier of the actor (user)
     * @return list of incidents reported by the given actor
     * @throws IllegalArgumentException if actorId is null or empty
     */
    @Override
    public List<Incident> findByActorId(String actorId) {
        if (actorId == null || actorId.trim().isEmpty())
            throw new IllegalArgumentException("Actor ID cannot be null or empty");

        return incidentRepository.findByUserId(actorId);
    }

    /**
     * Retrieves a specific Incident by its ID.
     *
     * @param id the unique identifier
     * @return the matching Incident
     * @throws IncidentNotFoundException if not found
     */
    @Override
    public Incident findById(long id) throws IncidentNotFoundException {
        if (id <= 0) throw new IllegalArgumentException("Incident ID must be positive");

        return incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));
    }

    /**
     * Persists a new Incident.
     *
     * @param createIncidentCommand the command containing incident details
     * @return the saved instance.
     * @throws ValidationException        if command validation fails
     * @throws LocationNotFoundException  if location coordinates are invalid
     * @throws ActorNotFoundException     if actor extraction fails
     * @throws DuplicateIncidentException if similar incident already exists
     */
    @Override
    public Incident create(CreateIncidentCommand createIncidentCommand) throws
            ValidationException, LocationNotFoundException, ActorNotFoundException, DuplicateIncidentException {

        final double longitude = createIncidentCommand.lon();
        final double latitude = createIncidentCommand.lat();

        validateCreateIncidentCommand(createIncidentCommand);

        try {
          final ActorId actorId = new ActorId("abc-123");
          final String title = createIncidentCommand.title();
          final String description = createIncidentCommand.description();
          final Set<UploadMediaCommand> media = createIncidentCommand.media();
          final Location location;

          try {
              CoordinatesCommand coordinatesCommand = new CoordinatesCommand(latitude, longitude);
              location = locationService.findByCoordinates(coordinatesCommand);
          } catch (RuntimeException e) {
              throw new LocationNotFoundException(
                      String.format("Location not found for coordinates: lat=%f, lon=%f",
                              createIncidentCommand.lat(), createIncidentCommand.lon()),
                      e);
        }

      final LocationId locationId = location.id();
      final Set<Media> uploadedMedia = objectStoragePort.uploadAll(media);


      /**
       * MODIFIED FOR DB
       */
      Incident incident = Incident.builder()
        .actorId(actorId)
        .locationId(locationId)
        .title(title)
        .description(description)
        .media(uploadedMedia)
        .engagementStats(new EngagementStats(0, 0, 0))
        .expiresAt(Instant.now().plus(Incident.TTL))
        .build();

            return incidentRepository.save(incident);
        } catch (ActorNotFoundException | LocationNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to create incident", e);
        }
    }

    /**
     * Updates an existing {@link Incident} by applying changes from a new instance.
     * Ensures the types of the original and new incident match.
     *
     * @param id                    ID of the happening to update.
     * @param createIncidentCommand the new values.
     * @return the updated and saved instance.
     * @throws IncidentNotFoundException if ID not found.
     * @throws ValidationException       if command validation fails
     * @throws ClassCastException        if the found happening is not an Incident
     */
    @Override
    public Incident update(long id, CreateIncidentCommand createIncidentCommand)
            throws IncidentNotFoundException, ValidationException {

      if (id <= 0) throw new IllegalArgumentException("Incident ID must be positive");
      validateCreateIncidentCommand(createIncidentCommand);

      try {
        final Incident existingIncident = findById(id);
        final String updatedTitle = createIncidentCommand.title();
        final String updatedDescription = createIncidentCommand.description();
        final Set<UploadMediaCommand> updatedMedia = createIncidentCommand.media();

        final Set<Media> uploadUpdatedMedia = objectStoragePort.uploadAll(updatedMedia);

        Incident updatedExistingOldIncident = existingIncident.toBuilder()
          .title(updatedTitle)
          .description(updatedDescription)
          .media(uploadUpdatedMedia)
          .build();

        return incidentRepository.save(updatedExistingOldIncident);

        } catch (IncidentNotFoundException e) {
            throw e;
        } catch (ClassCastException e) {
            throw new ValidationException("Found happening is not an incident with ID: " + id, e);
        } catch (Exception e) {
            throw new ValidationException("Failed to update incident", e);
        }
    }

    /**
     * Extends the expiration time of an incident by confirming it.
     *
     * @param incidentId the ID of the incident.
     * @return a new {@link Incident} instance with extended lifespan.
     * @throws IncidentNotFoundException         if the given ID does not point to an incident.
     * @throws IncidentAlreadyConfirmedException if the incident is already confirmed
     */
    @Override
    public Incident confirm(long incidentId)
            throws IncidentNotFoundException, IncidentAlreadyConfirmedException {

        if (incidentId <= 0) throw new IllegalArgumentException("Incident ID must be positive");

        try {
            Incident incident = findById(incidentId);
            incident.confirmIncident();

            return incidentRepository.save(incident);

        } catch (IncidentNotFoundException | IncidentAlreadyConfirmedException e) {
            throw e;
        } catch (ClassCastException e) {
            throw new IncidentNotFoundException("Found happening is not an incident with ID: " + incidentId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to confirm incident", e);
        }
    }

    /**
     * Finds the incident by ID, applies a denial, and saves it.
     * Denial increments the counters and may eventually mark
     * the incident as deleted.
     *
     * @param incidentId the incident identifier
     * @return the updated incident
     * @throws IncidentNotFoundException      if the incident is not found
     * @throws IncidentAlreadyDeniedException if the incident is already denied
     */
    @Override
    public Incident deny(long incidentId)
            throws IncidentNotFoundException, IncidentAlreadyDeniedException {

        if (incidentId <= 0) throw new IllegalArgumentException("Incident ID must be positive");

        try {
            Incident incident = findById(incidentId);
            incident.denyIncident();

            return incidentRepository.save(incident);

        } catch (IncidentNotFoundException | IncidentAlreadyDeniedException e) {
            throw e;
        } catch (ClassCastException e) {
            throw new IncidentNotFoundException("Found happening is not an incident with ID: " + incidentId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to confirm incident", e);
        }
    }

    /**
     * Deletes the incident if it is expired or otherwise considered deleted.
     * If the incident is still active, nothing happens.
     *
     * @param incidentId the incident identifier
     * @throws IncidentNotFoundException   if the incident is not found
     * @throws IncidentNotExpiredException if the incident is not expired/deleted
     */
    @Override
    public void deleteIfExpired(long incidentId)
            throws IncidentNotFoundException, IncidentNotExpiredException {

        if (incidentId <= 0) throw new IllegalArgumentException("Incident ID must be positive");

        try {
            Incident incident = findById(incidentId);
            if (incident.isDeleted()) {
                incidentRepository.deleteById(incidentId);
            } else {
                throw new IncidentNotExpiredException("Incident is not expired/deleted with ID: " + incidentId);
            }
        } catch (IncidentNotFoundException | IncidentNotExpiredException e) {
            throw e;
        } catch (ClassCastException e) {
            throw new IncidentNotFoundException("Found happening is not an incident with ID: " + incidentId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete expired incident", e);
        }

    }

    /**
     * Deletes a {@link Incident} by ID.
     *
     * @param incidentId the unique identifier of the entity to delete.
     * @throws IncidentNotFoundException if the incident is not found
     */
    @Override
    public void deleteById(long incidentId) throws IncidentNotExpiredException {
        if (incidentId <= 0) throw new IllegalArgumentException("Incident ID must be positive");

        if (!incidentRepository.existsById(incidentId))
            throw new IncidentNotFoundException("Incident not found with ID: " + incidentId);

        try {
            incidentRepository.deleteById(incidentId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete incident with ID: " + incidentId, e);
        }
    }

    private void validateCreateIncidentCommand(CreateIncidentCommand command) throws ValidationException {
        if (command == null) {
            throw new ValidationException("Create incident command cannot be null");
        }

        if (command.title() == null || command.title().trim().isEmpty()) {
            throw new ValidationException("Incident title cannot be null or empty");
        }

        if (command.title().length() > 255) {
            throw new ValidationException("Incident title cannot exceed 255 characters");
        }

        if (command.description() != null && command.description().length() > 1000) {
            throw new ValidationException("Incident description cannot exceed 1000 characters");
        }
    }
}
