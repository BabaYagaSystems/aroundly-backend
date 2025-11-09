package com.backend.adapter.inbound.mapper.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import com.backend.adapter.inbound.dto.media.MediaDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.inbound.mapper.IncidentMapper;
import com.backend.adapter.outbound.factory.MediaPreviewFactory;
import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.port.outbound.repo.LocationRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentPreviewDtoAssemblerTest {

  @Mock private IncidentMapper mapper;
  @Mock private LocationRepository locationRepository;
  @Mock private MediaPreviewFactory mediaPreviewFactory;
  @InjectMocks private IncidentPreviewDtoAssembler assembler;

  private Incident incident;
  private Location location;
  private Set<Media> media;

  @BeforeEach
  void setUp() {
    media = Set.of(new Media(1L, "file", "image/png"));
    incident = Incident.builder()
        .actorId(new ActorId("abc"))
        .locationId(new LocationId(42L))
        .title("title")
        .description("desc")
        .media(media)
        .build();
    location = new Location(new LocationId(42L), 21.5, 45.9, "address");
  }

  @Test
  void enrichesPreviewWithLocationAndGetMedia() {
    Set<MediaDto> previewMedia = Set.of(new MediaDto("preview"));
    IncidentPreviewResponseDto baseDto = IncidentPreviewResponseDto.builder()
        .title("title")
        .lat(0)
        .lon(0)
        .media(Set.of())
        .build();

    when(mapper.toIncidentPreviewResponseDto(incident)).thenReturn(baseDto);
    when(locationRepository.findById(42L)).thenReturn(location);
    when(mediaPreviewFactory.build(media)).thenReturn(previewMedia);

    IncidentPreviewResponseDto result = assembler.toPreviewDto(incident);

    assertEquals(location.latitude(), result.lat());
    assertEquals(location.longitude(), result.lon());
    assertSame(previewMedia, result.media());
    assertEquals(baseDto.title(), result.title());
  }
}

