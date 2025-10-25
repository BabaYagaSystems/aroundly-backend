package com.backend.adapter.outbound.repo;

import com.backend.adapter.outbound.entity.IncidentEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentPersistenceRepository extends JpaRepository<IncidentEntity, Long> {

  @Query(value = """
    SELECT i.*, 
           ST_Distance(
               ST_SetSRID(ST_MakePoint(l.lng, l.lat), 4326)::geography,
               ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
           ) as distance_meters
    FROM incidents i
    JOIN locations l ON l.id = i.location_id
    WHERE ST_DWithin(
           ST_SetSRID(ST_MakePoint(l.lng, l.lat), 4326)::geography,
           ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
           :radiusMeters
    )
    ORDER BY distance_meters ASC
    LIMIT 100
    """, nativeQuery = true)
  List<IncidentEntity> findAllInGivenRange(
      @Param("lat") double lat,
      @Param("lon") double lon,
      @Param("radiusMeters") double radiusMeters);
}
