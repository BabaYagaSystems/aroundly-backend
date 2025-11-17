package com.backend.adapter.inbound.mapper;

import com.backend.adapter.inbound.dto.request.CoordinatesRequestDto;
import com.backend.adapter.inbound.dto.request.RadiusRequestDto;
import com.backend.adapter.inbound.dto.response.AddressResponseDto;
import com.backend.adapter.inbound.dto.response.CoordinateResponseDto;
import com.backend.domain.location.Location;
import com.backend.port.inbound.commands.CoordinatesCommand;
import com.backend.port.inbound.commands.RadiusCommand;


public final class LocationMapper {

  public static RadiusCommand toRadiusCommand(RadiusRequestDto radiusRequestDto) {
    return new RadiusCommand(
        radiusRequestDto.lat(),
        radiusRequestDto.lon(),
        radiusRequestDto.radius());
  }

  public static CoordinatesCommand toCoordinatesCommand(CoordinatesRequestDto coordinatesRequestDto) {
    return new CoordinatesCommand(
        coordinatesRequestDto.lat(),
        coordinatesRequestDto.lon());
  }

  public static CoordinateResponseDto toCoordinateResponseDto(Location location) {
    return new CoordinateResponseDto(
        location.latitude(),
        location.longitude(),
        location.address());
  }

  public static AddressResponseDto toAddressResponseDto(String address) {
    return new AddressResponseDto(address);
  }

}
