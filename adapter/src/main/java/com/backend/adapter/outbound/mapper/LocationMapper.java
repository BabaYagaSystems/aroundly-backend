package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;


public final class LocationMapper {

  public static LocationEntity mapToEntity(Location domain) {
    return LocationEntity.builder()
      .lat(domain.latitude())
      .lng(domain.longitude())
      .addressText(domain.address())
      .build();
  }

  public static Location mapToDomain(LocationEntity entity) {
    return new Location(
      new LocationId(entity.getId()),
      entity.getLng(),
      entity.getLat(),
      entity.getAddressText());
  }
}
