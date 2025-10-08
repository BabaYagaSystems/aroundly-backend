package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.HappeningEntity;
import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.entity.MediaEntity;
import com.backend.adapter.outbound.mapper.IncidentEntityMapper;
import com.backend.adapter.outbound.mapper.MediaEntityMapper;
import com.backend.adapter.outbound.repo.*;
import com.backend.domain.happening.Happening;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.port.outbound.repo.IncidentRepository;
import com.backend.port.outbound.repo.LocationRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class FakeIncidentPersistenceRepository implements IncidentRepository {

    private final IncidentPersistenceRepository incidentPersistenceRepository;
    private final HappeningPersistenceRepository happeningPersistenceRepository;
    private final LocationRepository locationRepository;
    private final MediaEntityMapper mediaEntityMapper;
    private final IncidentEntityMapper incidentEntityMapper;
    private final LocationPersistenceRepository locationPersistenceRepository;

    private final Map<Long, Happening> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Incident save(Incident incident) {
        // FIXED: Check if this is an update (has ID) or new incident (no ID)
        boolean isUpdate = incident.getId() != null;

        if (isUpdate) {
            return updateExistingIncident(incident);
        } else {
            return createNewIncident(incident);
        }
    }

    private Incident createNewIncident(Incident incident) {
        // Generate new ID for in-memory storage
        long id = idGenerator.getAndIncrement();
        storage.put(id, incident);

        // Get location entity
        LocationEntity locationEntity = locationPersistenceRepository
                .findById(incident.locationId().value())
                .orElseThrow(() -> new IllegalStateException("Location not found"));

        // Build media entities
        Set<MediaEntity> mediaEntities = incident.media().stream()
                .map(mediaEntityMapper::toEntity)
                .collect(Collectors.toSet());

        // Build happening entity
        HappeningEntity happeningEntity = HappeningEntity.builder()
                .title(incident.getTitle())
                .description(incident.getDescription())
                .media(mediaEntities)
                .createdAt(LocalDateTime.now())
                .location(locationEntity)
                .build();

        // Set bidirectional relationship
        for (MediaEntity mediaEntity : mediaEntities) {
            mediaEntity.setHappeningEntity(happeningEntity);
        }

        // Save happening first to get its ID
        HappeningEntity savedHappening = happeningPersistenceRepository.save(happeningEntity);

        // Build and save incident entity
        IncidentEntity incidentEntity = IncidentEntity.builder()
                .happening(savedHappening)
                .timePosted(LocalDateTime.now())
                .range(1000)
                .confirms(incident.getEngagementStats().confirms())
                .denies(incident.getEngagementStats().denies())
                .expiresAt(LocalDateTime.ofInstant(incident.expiresAt(), ZoneOffset.UTC))
                .build();

        IncidentEntity savedIncident = incidentPersistenceRepository.save(incidentEntity);

        // Return incident with the database-generated happening ID
        return incident.toBuilder()
                .id(savedHappening.getId())
                .build();
    }

    private Incident updateExistingIncident(Incident incident) {
        Long happeningId = incident.getId();

        // Update in-memory storage
        storage.put(happeningId, incident);

        // Find existing incident entity
        IncidentEntity existingIncidentEntity = incidentPersistenceRepository
                .findByHappeningId(happeningId)
                .orElseThrow(() -> new IllegalStateException(
                        "Incident not found for happening ID: " + happeningId));

        // Update the incident entity fields
        existingIncidentEntity.setConfirms(incident.getEngagementStats().confirms());
        existingIncidentEntity.setDenies(incident.getEngagementStats().denies());
        existingIncidentEntity.setExpiresAt(
                LocalDateTime.ofInstant(incident.expiresAt(), ZoneOffset.UTC));

        // Update the happening entity
        HappeningEntity happeningEntity = existingIncidentEntity.getHappening();
        happeningEntity.setTitle(incident.getTitle());
        happeningEntity.setDescription(incident.getDescription());

        // Save the updated entities (cascade should handle happening)
        incidentPersistenceRepository.save(existingIncidentEntity);

        return incident;
    }

    @Override
    public Optional<Happening> findById(long id) {
        // Try to find an incident by happening ID
        Optional<IncidentEntity> incidentOpt = incidentPersistenceRepository.findByHappeningId(id);

        if (incidentOpt.isPresent()) {
            Incident incident = incidentEntityMapper.toIncident(incidentOpt.get());
            // Update in-memory storage
            storage.put(id, incident);
            return Optional.of(incident);
        }

        // Fallback to generic happening
        return happeningPersistenceRepository.findById(id)
                .map(happeningEntity -> {
                    throw new UnsupportedOperationException(
                            "Mapping plain HappeningEntity to domain Happening not yet implemented");
                });
    }

    @Override
    public boolean existsById(long happeningId) {
        return incidentPersistenceRepository.findByHappeningId(happeningId).isPresent();
    }

    @Override
    public List<Incident> findAllInGivenRange(double lat0, double lon0, double radiusMeters) {
        final double radiusKm = radiusMeters / 1000.0;

        // Query from database instead of in-memory storage
        return incidentPersistenceRepository.findAll().stream()
                .map(incidentEntityMapper::toIncident)
                .filter(i -> {
                    LocationId locationId = i.locationId();
                    Location location = locationRepository.findById(locationId.value());
                    if (location == null) return false;

                    double lat = location.latitude();
                    double lon = location.longitude();

                    return haversineKm(lat0, lon0, lat, lon) <= radiusKm;
                })
                .toList();
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);

        // Delete from database
        incidentPersistenceRepository.findByHappeningId(id)
                .ifPresent(incidentEntity -> {
                    incidentPersistenceRepository.delete(incidentEntity);
                    // Happening will be cascade deleted if configured
                });
    }

    @Override
    public List<Happening> findByUserId(String userId) {
        // TODO: Implement proper query by user/actor ID
        return incidentPersistenceRepository.findAll().stream()
                .map(incidentEntityMapper::toIncident)
                .filter(Objects::nonNull)
                .map(i -> (Happening) i)
                .toList();
    }
}