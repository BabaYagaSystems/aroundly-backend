package com.backend.port.inbound;

import com.backend.domain.location.Location;
import com.backend.port.inbound.commands.CoordinatesCommand;

/**
 * Defines use cases for managing and retrieving {@link Location} objects.
 */
public interface LocationUseCase {

    /**
     * Finds a location by its unique identifier.
     *
     * @param locationId the identifier of the location
     * @return the location with the given identifier
     */
    Location findById(long locationId);

    /**
     * Finds a location by its geographic coordinates.
     *
     * @param coordinatesCommand the command containing latitude and longitude
     * @return the location at the given coordinates
     */
    Location findByCoordinates(CoordinatesCommand coordinatesCommand);

    /**
     * Performs reverse geocoding to return a readable address string for the
     * provided coordinates.
     *
     * @param coordinatesCommand the latitude/longitude payload
     * @return address string supplied by the backing geocoding provider
     */
    String getAddress(CoordinatesCommand coordinatesCommand);
}
