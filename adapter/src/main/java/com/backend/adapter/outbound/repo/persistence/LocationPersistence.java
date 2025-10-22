package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.repo.LocationPersistenceRepository;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.port.outbound.repo.LocationRepository;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class LocationPersistence implements LocationRepository {

  private final LocationPersistenceRepository locationPersistenceRepository;

  @Override
  public Location save(Location location) {
    LocationEntity locationEntity = toLocationEntity(location);

    // If ID is 0 or null, let database generate
    if (location.id().value() == 0L) {
      locationEntity.setId(0L);
    }

    LocationEntity savedEntity = locationPersistenceRepository.save(locationEntity);

    return toLocation(savedEntity);
  }

  @Override
  public Location findById(long id) {
    LocationEntity locationEntity = locationPersistenceRepository.findById(id)
        .orElseThrow(() -> new IllegalStateException("Location not found"));

    return toLocation(locationEntity);
  }

  @Override
  public Optional<Location> findByCoordinate(double latitude, double longitude) {
    return locationPersistenceRepository.findByCoordinate(latitude, longitude);
  }

  @Override
  public void deleteById(long id) {
    locationPersistenceRepository.deleteById(id);
  }

  private LocationEntity toLocationEntity(Location location) {
    return LocationEntity.builder()
        .lat(location.latitude())
        .lng(location.longitude())
        .addressText(location.address())
        .build();
  }

  private Location toLocation(LocationEntity locationEntity) {
    return new Location(
        new LocationId(locationEntity.getId()),
        locationEntity.getLng(),
        locationEntity.getLat(),
        locationEntity.getAddressText());
  }
}
