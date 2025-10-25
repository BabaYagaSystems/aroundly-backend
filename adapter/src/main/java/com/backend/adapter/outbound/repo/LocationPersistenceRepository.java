package com.backend.adapter.outbound.repo;

import com.backend.adapter.outbound.entity.LocationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationPersistenceRepository extends JpaRepository<LocationEntity, Long> {

  Optional<LocationEntity> findByLatAndLng(double lat, double lng);
}
