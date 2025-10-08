package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationEntityMapper {

    // Domain → Entity
    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "latitude", target = "lat")
    @Mapping(source = "longitude", target = "lng")
    @Mapping(source = "address", target = "addressText")
    LocationEntity toLocationEntity(Location location);

    // Entity → Domain
    @Mapping(source = "id", target = "id.value")
    @Mapping(source = "lat", target = "latitude")
    @Mapping(source = "lng", target = "longitude")
    @Mapping(source = "addressText", target = "address")
    Location toLocation(LocationEntity locationEntity);

    // Helper method for LocationId mapping (handles null)
    default LocationId map(Long value) {
        return value == null ? null : new LocationId(value);
    }

    default Long map(LocationId locationId) {
        return locationId == null || locationId.value() == null ? null : locationId.value();
    }
}