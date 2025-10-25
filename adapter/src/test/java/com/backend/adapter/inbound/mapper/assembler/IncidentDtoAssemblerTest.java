package com.backend.adapter.inbound.mapper.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.backend.adapter.inbound.dto.media.MediaDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import com.backend.adapter.inbound.mapper.IncidentMapper;
import com.backend.adapter.inbound.mapper.IncidentMapperImpl;
import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.port.outbound.repo.LocationRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = IncidentMapperImpl.class)
public class IncidentDtoAssemblerTest {

  @Mock private IncidentMapper mapper;
  @Mock private LocationRepository locationRepository;
  @InjectMocks private IncidentDtoAssembler assembler;


  private Incident incident;
  private Media media;
  private MediaDto mediaDto;

  @BeforeEach
  void setUp() {
    mediaDto = new MediaDto("file");
    media = new Media(3L, "file", "type");
    incident = Incident.builder()
      .actorId(new ActorId("abc-123"))
      .locationId(new LocationId(202L))
      .title("title")
      .description("description")
      .media(Set.of(media))
      .build();

    incident.confirmIncident();
    incident.confirmIncident();
    incident.denyIncident();
  }

  @Test
  @Disabled
  void testEnrichAllFields() {

    IncidentDetailedResponseDto expected = IncidentDetailedResponseDto.builder()
      .title("title")
      .description("description")
      .media(Set.of(mediaDto))
      .confirm(2)
      .deny(1)
      .consecutiveDenies(1)
      .like(0)
      .dislike(0)
      .build();

    Location location = new Location(
      new LocationId(202L),
      13.4050, 52.5200,
      "address");

    when(locationRepository.findById(202L)).thenReturn(location);
    when(mapper.toIncidentDetailedResponseDto(incident)).thenReturn(expected);

    IncidentDetailedResponseDto toDto = assembler.toDetailedDto(incident);

    assertEquals(expected.title(), toDto.title());
    assertEquals(expected.description(), toDto.description());
    assertEquals(expected.media(), toDto.media());
    assertEquals(expected.confirm(), toDto.confirm());
    assertEquals(expected.deny(), toDto.deny());
    assertEquals(expected.consecutiveDenies(), toDto.consecutiveDenies());
    assertEquals(expected.like(), toDto.like());
    assertEquals(expected.dislike(), toDto.dislike());
    assertEquals(location.address(), toDto.address());
    assertEquals(location.latitude(), toDto.lat());
    assertEquals(location.longitude(), toDto.lon());
  }

}
