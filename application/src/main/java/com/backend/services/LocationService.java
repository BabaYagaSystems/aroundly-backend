package com.backend.services;

import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.port.inbound.LocationUseCase;
import com.backend.port.inbound.commands.CoordinatesCommand;
import com.backend.port.outbound.repo.LocationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service layer implementation of {@link LocationUseCase},
 * providing access to location data through the repository.
 */
@Service
public class LocationService implements LocationUseCase {

    private final LocationRepository locationRepository;
    private final String mapboxToken;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocationService(
            LocationRepository locationRepository,
            @Value("${mapbox.token}") String mapboxToken) {

        this.locationRepository = locationRepository;
        this.mapboxToken = mapboxToken;
    }

    /**
     * Finds a location by its unique identifier.
     *
     * @param locationId id of the location
     * @return the matching location, or null if not found
     */
    @Override
    public Location findById(long locationId) {
        return locationRepository.findById(locationId);
    }

    /**
     * Finds a location by its latitude and longitude coordinates.
     *
     * @param coordinatesCommand command containing latitude and longitude
     * @return the matching location, or null if not found
     */
    @Override
    public Location findByCoordinates(CoordinatesCommand coordinatesCommand) {
        final double latitude = coordinatesCommand.lat();
        final double longitude = coordinatesCommand.lon();

        return locationRepository
                .findByCoordinate(latitude, longitude)
                .orElseGet(() -> createLocation(longitude, latitude));
    }


    /// WHY MANUALLY ASSIGN THE ID???

    private Location createLocation(double longitude, double latitude) {
        String address = reverseGeocode(longitude, latitude);
        Location newLocation = new Location(
                new LocationId(0L),
//            locationIdGenerator.nextId(),
            longitude,
            latitude,
            address);

        return locationRepository.save(newLocation);
    }

    private String reverseGeocode(double longitude, double latitude) {
        final String language = "en";
        String uri = String.format(
                "https://api.mapbox.com/geocoding/v5/mapbox.places/%f,%f.json?language=%s&limit=1&access_token=%s",
                longitude,
                latitude,
                language,
                mapboxToken);

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new RuntimeException("Rate limited by Mapbox");
            }
            if (response.statusCode() / 100 != 2) {
                throw new RuntimeException("Mapbox error: " + response.body());
            }

            JsonNode node = objectMapper.readTree(response.body());
            JsonNode features = node.path("features");
            if (features.isArray() && !features.isEmpty()) {
                return features.get(0).path("place_name").asText("Unknown address");
            }
            return "Unknown address";

        } catch (Exception e) {
            throw new RuntimeException("Reverse geocode failed", e);
        }
    }
}
