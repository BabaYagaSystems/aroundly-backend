package com.backend.adapter.outbound.repo.persistence;

import static com.backend.adapter.outbound.mapper.LocationMapper.mapToDomain;
import static com.backend.adapter.outbound.mapper.LocationMapper.mapToEntity;

import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.mapper.LocationMapper;
import com.backend.adapter.outbound.repo.LocationPersistenceRepository;
import com.backend.domain.location.Location;
import com.backend.port.outbound.repo.LocationRepository;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LocationPersistence implements LocationRepository {

  private final LocationPersistenceRepository locationPersistenceRepository;

  @Override
  public Location save(Location location) {
    LocationEntity locationEntity = mapToEntity(location);
    LocationEntity savedEntity = locationPersistenceRepository.save(locationEntity);

    return mapToDomain(savedEntity);
  }

  @Override
  public Location findById(long id) {
    LocationEntity locationEntity = locationPersistenceRepository.findById(id)
        .orElseThrow(() -> new IllegalStateException("Location not found"));

    return mapToDomain(locationEntity);
  }

  @Override
  public Optional<Location> findByCoordinate(double latitude, double longitude) {
    return locationPersistenceRepository
        .findByLatAndLng(latitude, longitude)
        .map(LocationMapper::mapToDomain);
  }

  @Override
  public void deleteById(long id) {
    locationPersistenceRepository.deleteById(id);
  }
}
