package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.HappeningEntity;
import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.entity.MediaEntity;
import com.backend.adapter.outbound.mapper.MediaEntityMapper;
import com.backend.adapter.outbound.repo.*;
import com.backend.domain.happening.Happening;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;

import com.backend.port.outbound.repo.IncidentRepository;
import com.backend.port.outbound.repo.LocationRepository;
import java.time.LocalDateTime;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class FakeIncidentPersistenceRepository implements IncidentRepository {

    private final IncidentPersistenceRepository incidentPersistenceRepository; //For DB
    private final HappeningPersistenceRepository happeningPersistenceRepository;
    private final LocationRepository locationRepository;

    private final MediaEntityMapper mediaEntityMapper;
    
    private final Map<Long, Happening> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final LocationPersistenceRepository locationPersistenceRepository;


    /**
     * Since the Happening is abstract it needs to be created by each child entity that implements it
     * Also it needs to be mapped manually since MapStruct is not working with abstract classes
     * */
    @Override
    public Incident save(Incident incident) {
        long id = idGenerator.getAndIncrement();
        storage.put(id, incident);

        /// THIS I THINK WILL NEED TO BE MOVED TO ONE OF THE LOCATION REPOSITORY
        LocationEntity locationEntity = locationPersistenceRepository
                .findById(incident.locationId().value())
                .orElseThrow(() -> new IllegalStateException("Location not found"));

        Set<MediaEntity> mediaEntities = incident.media().stream()
            .map(mediaEntityMapper::toEntity)
            .collect(Collectors.toSet());

        HappeningEntity happeningEntity = HappeningEntity.builder()
                .title(incident.getTitle())
                .description(incident.getDescription())
                .media(mediaEntities)
                .createdAt(LocalDateTime.now())
                .location(locationEntity)
                .build();

        for (MediaEntity mediaEntity : mediaEntities) {
            mediaEntity.setHappeningEntity(happeningEntity);
        }

        happeningPersistenceRepository.save(happeningEntity);

        /**
         * OLD VERSION USING THE MAPPER
         * BUT THE HAPPENING WILL NOT BE AUTOMATICALLY SET BECAUSE IT IS ABSTRACT
         * */
//        IncidentEntity incidentEntity = incidentEntityMapper.toIncidentEntity(incident); //For DB

        /// THIS BUILDER MAY NEED TO BE TRANSFERRED
        IncidentEntity incidentEntity = IncidentEntity.builder()
                .happening(happeningEntity)
                .timePosted(LocalDateTime.now())
                .range(1000) /// WHERE FROM DO WE NEED TO GET THIS VALUE
                .confirms(incident.getEngagementStats().confirms())
                .denies(incident.getEngagementStats().denies())
                .build();

        incidentPersistenceRepository.save(incidentEntity); //FOr DB

        return incident;
    }

    @Override
    public Optional<Happening> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(long happeningId) {
      return storage.get(happeningId) != null;
    }

    @Override
    public List<Incident> findAllInGivenRange(double lat0, double lon0, double radiusMeters) {
        final double radiusKm = radiusMeters / 1000.0;

        return storage.values().stream()
            .filter(h -> h instanceof Incident)
            .map(Incident.class::cast)
            .filter(i -> {
                LocationId locationId = i.locationId();
                Location location = locationRepository.findById(locationId.value());
                if (location == null) return false;

                double lat = location.latitude();
                double lon = location.longitude();

                return haversineKm(lat0, lon0, lat, lon) <= radiusKm;
            })
            .toList();
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon/2) * Math.sin(dLon/2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public List<Happening> findByUserId(String userId) {
        return storage.values().stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
