package com.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.port.inbound.commands.CoordinatesCommand;
import com.backend.port.outbound.repo.LocationRepository;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

  @Mock private LocationRepository locationRepository;
  @Mock private HttpClient httpClient;

  @InjectMocks private LocationService locationService;

  @BeforeEach
  void setUp() throws Exception {
    Field httpClientField = LocationService.class.getDeclaredField("httpClient");
    httpClientField.setAccessible(true);
    httpClientField.set(locationService, httpClient);
  }

  @Test
  void findByCoordinatesReturnsExistingLocation() {
    CoordinatesCommand command = new CoordinatesCommand(10.0, 20.0);
    Location existing = new Location(new LocationId(7L), 20.0, 10.0, "Cached address");

    when(locationRepository.findByCoordinate(command.lat(), command.lon()))
        .thenReturn(Optional.of(existing));

    Location result = locationService.findByCoordinates(command);

    assertSame(existing, result);
    verify(locationRepository, never()).save(any());
    verifyNoInteractions(httpClient);
  }

  @Test
  void findByCoordinatesCreatesLocationWhenMissing() throws Exception {
    CoordinatesCommand command = new CoordinatesCommand(11.5, 22.5);
    when(locationRepository.findByCoordinate(command.lat(), command.lon()))
        .thenReturn(Optional.empty());

    HttpResponse<String> response = Mockito.mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn("""
        {"features":[{"place_name":"Generated address"}]}
        """);
    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    when(locationRepository.save(any(Location.class)))
        .thenAnswer(invocation -> {
          Location saved = invocation.getArgument(0);
          return new Location(
              new LocationId(99L),
              saved.longitude(),
              saved.latitude(),
              saved.address());
        });

    Location result = locationService.findByCoordinates(command);

    assertEquals("Generated address", result.address());
    verify(locationRepository).save(any(Location.class));
    verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }
}
