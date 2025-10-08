package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.ClientEntity;
import com.backend.adapter.outbound.entity.HappeningEntity;
import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.LocationId;
import org.mapstruct.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface IncidentEntityMapper {

    // Domain → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "happening", source = "incident")
    @Mapping(source = "engagementStats.confirms", target = "confirms")
    @Mapping(source = "engagementStats.denies", target = "denies")
    @Mapping(source = "expiresAt", target = "expiresAt", qualifiedByName = "instantToLocalDateTime")
    @Mapping(target = "timePosted", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "range", constant = "0.0")
    IncidentEntity toIncidentEntity(Incident incident);

    // Helper: Build HappeningEntity from Incident
    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "actorId", target = "client")
    @Mapping(source = "locationId", target = "location")
    @Mapping(target = "media", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    HappeningEntity incidentToHappeningEntity(Incident incident);

    // Helper: Build ClientEntity from ActorId
    @Mapping(source = "id", target = "keycloakId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fcmToken", ignore = true)
    @Mapping(target = "range", ignore = true)
    ClientEntity actorIdToClientEntity(ActorId actorId);

    // Helper: Build LocationEntity from LocationId
    @Mapping(source = "value", target = "id")
    @Mapping(target = "lat", constant = "0.0")
    @Mapping(target = "lng", constant = "0.0")
    @Mapping(target = "addressText", ignore = true)
    LocationEntity locationIdToLocationEntity(LocationId locationId);

    // Entity → Domain
    @Mapping(source = "happening.id", target = "id")
    @Mapping(source = "happening.title", target = "title")
    @Mapping(source = "happening.description", target = "description")
    @Mapping(source = "happening.client", target = "actorId")
    @Mapping(source = "happening.location", target = "locationId")
    @Mapping(target = "engagementStats",
            expression = "java(new com.backend.domain.reactions.EngagementStats(incidentEntity.getConfirms(), incidentEntity.getDenies(), 0))")
    @Mapping(source = "expiresAt", target = "expiresAt", qualifiedByName = "localDateTimeToInstant")
    @Mapping(target = "media", expression = "java(java.util.Collections.emptySet())")
    Incident toIncident(IncidentEntity incidentEntity);

    // Helper: ClientEntity → ActorId
    @Mapping(source = "keycloakId", target = "id")
    ActorId clientEntityToActorId(ClientEntity clientEntity);

    // Helper: LocationEntity → LocationId
    @Mapping(source = "id", target = "value")
    LocationId locationEntityToLocationId(LocationEntity locationEntity);

    // Time converters
    @Named("instantToLocalDateTime")
    static LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Named("localDateTimeToInstant")
    static Instant localDateTimeToInstant(LocalDateTime time) {
        return time == null ? null : time.toInstant(ZoneOffset.UTC);
    }
}