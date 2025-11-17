package com.backend.adapter.inbound.rest;

import static com.backend.adapter.inbound.mapper.LocationMapper.toAddressResponseDto;
import static com.backend.adapter.inbound.mapper.LocationMapper.toCoordinatesCommand;

import com.backend.adapter.inbound.dto.request.CoordinatesRequestDto;
import com.backend.adapter.inbound.dto.response.AddressResponseDto;
import com.backend.adapter.inbound.rest.exception.incident.InvalidCoordinatesException;
import com.backend.port.inbound.LocationUseCase;
import com.backend.port.inbound.commands.CoordinatesCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints related to locations, currently allowing
 * consumers to convert latitude/longitude pairs into human-readable addresses.
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/locations")
@Tag(name = "locations", description = "Location management")
public class LocationController {

  private final LocationUseCase locationUseCase;

  public LocationController(LocationUseCase locationUseCase) {
    this.locationUseCase = locationUseCase;
  }

  /**
   * Resolves a formatted address for the given coordinates by delegating to the
   * location use case and returning the result as a DTO, handling invalid input
   * by responding with {@code 400 Bad Request}.
   */
  @GetMapping("/address")
  @Operation(
      summary = "Address based on coordinates",
      description = "Finds the address based on given coordinates"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Address was found and retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid coordinates")
  })
  public ResponseEntity<AddressResponseDto> getAddress(
      @ModelAttribute @Valid final CoordinatesRequestDto coordinatesRequestDto) {

    try {
      final CoordinatesCommand coordinatesCommand = toCoordinatesCommand(coordinatesRequestDto);
      final String address = locationUseCase.getAddress(coordinatesCommand);
      final AddressResponseDto addressResponseDto = toAddressResponseDto(address);

      return ResponseEntity.ok(addressResponseDto);

    } catch (final InvalidCoordinatesException e) {
      log.warn("Invalid coordinates provided: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }
}
