package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.mapper.LocationEntityMapper;
import com.backend.adapter.outbound.repo.LocationPersistenceRepository;
import com.backend.domain.location.Location;
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

  /**
   *        QUESTION TO ASK : WHY CUSTOM NEXT ID GENERATOR NEEDED???
   * */

  @Override
  public Location save(Location location) {
    LocationEntity locationEntity = locationEntityMapper.toLocationEntity(location);

    // If ID is 0 or null, let database generate
    if (location.id().value() == 0L) {
      locationEntity.setId(0L);
    }

    LocationEntity savedEntity = locationPersistenceRepository.save(locationEntity);

    // Map back with generated ID
    Location savedLocation = locationEntityMapper.toLocation(savedEntity);
    storage.put(savedLocation.id().value(), savedLocation);

    return savedLocation;
  }

  @Override
  public Location findById(long id) {
    return storage.get(id);
  }

  @Override
  public Optional<Location> findByCoordinate(double latitude, double longitude) {
    return storage.values().stream()
        .filter(loc -> loc.latitude() == latitude && loc.longitude() == longitude)
        .findFirst();
  }

  @Override
  public void deleteById(long id) {
    storage.remove(id);
  }
}
