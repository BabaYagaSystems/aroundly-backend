package com.backend.adapter.outbound.repo;

import com.backend.adapter.outbound.entity.ReactionEntity;
import com.backend.port.inbound.commands.ReactionSummary;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository managing durable reaction records stored in the relational database.
 */
@Repository
public interface ReactionPersistenceRepository extends JpaRepository<ReactionEntity, Long> {

  Optional<ReactionEntity> findByIncidentId(long incidentId);
}
