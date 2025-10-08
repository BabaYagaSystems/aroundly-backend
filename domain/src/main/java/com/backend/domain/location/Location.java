package com.backend.domain.location;

/**
 * Represents a geographic location with an identifier,
 * longitude, and latitude.
 *
 * @param id        the unique identifier of the location (can be null before persistence)
 * @param longitude the longitude coordinate
 * @param latitude  the latitude coordinate
 * @param address   the address of location
 */
public record Location(LocationId id, double longitude, double latitude, String address) {

  public Location {
    if (latitude < -90 || latitude > 90)
      throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");

    if (longitude < -180 || longitude > 180)
      throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");

    // Allow null/empty address for new locations before geocoding
    if (address != null && address.trim().isEmpty()) {
      throw new IllegalArgumentException("Address cannot be empty string");
    }
  }
}
