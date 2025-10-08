package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.mapper.LocationEntityMapper;
import com.backend.adapter.outbound.repo.LocationPersistenceRepository;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.port.outbound.repo.LocationRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class FakeLocationPersistenceRepository implements LocationRepository {

  private final LocationPersistenceRepository locationPersistenceRepository;
  private final LocationEntityMapper locationEntityMapper;
  private final Map<Long, Location> storage = new ConcurrentHashMap<>();

  @Override
  public Location save(Location location) {
    LocationEntity locationEntity = locationEntityMapper.toLocationEntity(location);

    // FIXED: If ID is null, don't set it - let JPA generate
    if (location.id() == null || location.id().value() == null) {
      locationEntity.setId(null);
    }

    LocationEntity savedEntity = locationPersistenceRepository.save(locationEntity);

    // Map back with generated ID
    Location savedLocation = locationEntityMapper.toLocation(savedEntity);
//    storage.put(savedLocation.id().value(), savedLocation);

    return savedLocation;
  }

  @Override
  public Location findById(long id) {
    LocationEntity entity = locationPersistenceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Location not found for ID " + id));
    return locationEntityMapper.toLocation(entity);
  }

  @Override
  public Optional<Location> findByCoordinate(double latitude, double longitude) {
    // FIXED: Also check database, not just in-memory storage
    return locationPersistenceRepository.findAll().stream()
            .filter(e -> Math.abs(e.getLat() - latitude) < 0.0001 &&
                    Math.abs(e.getLng() - longitude) < 0.0001)
            .findFirst()
            .map(locationEntityMapper::toLocation);
  }

  @Override
  public void deleteById(long id) {
    locationPersistenceRepository.deleteById(id);
    storage.remove(id);
  }
}