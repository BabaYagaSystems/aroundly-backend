package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.ClientEntity;
import com.backend.domain.actor.Actor;
import com.backend.domain.actor.ActorId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientEntityMapper {

    @Mapping(source = "id.id", target = "keycloakId")
    @Mapping(target = "id", ignore = true) // Let JPA generate
    @Mapping(target = "fcmToken", ignore = true)
    @Mapping(target = "range", ignore = true)
    ClientEntity toClientEntity(Actor actor);

    @Mapping(source = "keycloakId", target = "id.id")
    Actor toActor(ClientEntity clientEntity);

    // Helper methods for ActorId conversion
    default String mapActorId(ActorId actorId) {
        return actorId == null ? null : actorId.id();
    }

    default ActorId mapString(String id) {
        return id == null ? null : new ActorId(id);
    }
}