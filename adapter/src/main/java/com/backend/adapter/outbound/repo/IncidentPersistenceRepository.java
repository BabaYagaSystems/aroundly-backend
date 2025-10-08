package com.backend.adapter.outbound.repo;

import com.backend.adapter.outbound.entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncidentPersistenceRepository extends JpaRepository<IncidentEntity, Long> {

    Optional<IncidentEntity> findByHappeningId(Long happeningId);
}
