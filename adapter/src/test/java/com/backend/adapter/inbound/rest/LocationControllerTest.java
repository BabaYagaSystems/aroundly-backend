package com.backend.adapter.inbound.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.backend.adapter.inbound.dto.request.CoordinatesRequestDto;
import com.backend.adapter.inbound.dto.response.AddressResponseDto;
import com.backend.adapter.inbound.rest.exception.incident.InvalidCoordinatesException;
import com.backend.port.inbound.LocationUseCase;
import com.backend.port.inbound.commands.CoordinatesCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

  @Mock private LocationUseCase locationUseCase;

  @InjectMocks private LocationController locationController;

  private CoordinatesRequestDto requestDto;
  private CoordinatesCommand command;

  @BeforeEach
  void setUp() {
    requestDto = new CoordinatesRequestDto(51.5, -0.12);
    command = new CoordinatesCommand(requestDto.lat(), requestDto.lon());
  }

  @Test
  void getAddressReturnsOkResponse() {
    when(locationUseCase.getAddress(command)).thenReturn("London address");

    ResponseEntity<AddressResponseDto> response = locationController.getAddress(requestDto);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("London address", response.getBody().address());
    verify(locationUseCase).getAddress(command);
    verifyNoMoreInteractions(locationUseCase);
  }

  @Test
  void getAddressReturnsBadRequestOnInvalidCoordinates() {
    when(locationUseCase.getAddress(command))
        .thenThrow(new InvalidCoordinatesException("invalid coordinate pair"));

    ResponseEntity<AddressResponseDto> response = locationController.getAddress(requestDto);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNull(response.getBody());
    verify(locationUseCase).getAddress(command);
    verifyNoMoreInteractions(locationUseCase);
  }
}
