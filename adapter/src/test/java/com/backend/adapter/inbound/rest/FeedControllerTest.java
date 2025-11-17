package com.backend.adapter.inbound.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.adapter.inbound.dto.request.RadiusRequestDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.inbound.mapper.IncidentResponseMapper;
import com.backend.domain.actor.UserId;
import com.backend.domain.happening.Incident;
import com.backend.domain.happening.IncidentId;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.domain.reactions.EngagementStats;
import com.backend.domain.reactions.SentimentEngagement;
import com.backend.port.inbound.IncidentUseCase;
import com.backend.port.inbound.commands.RadiusCommand;
import com.backend.services.exceptions.InvalidCoordinatesException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

  @Mock private IncidentUseCase incidentUseCase;
  @Mock private IncidentResponseMapper incidentResponseMapper;

  @InjectMocks private FeedController feedController;

  @Test
  void findAllInGivenRangeReturnsMappedIncidents() {
    RadiusRequestDto request = new RadiusRequestDto(10.0, 20.0, 500);
    Incident incident = sampleIncident();
    IncidentPreviewResponseDto preview = samplePreview();

    when(incidentUseCase.findAllInGivenRange(any(RadiusCommand.class)))
        .thenReturn(List.of(incident));
    when(incidentResponseMapper.toIncidentPreviewResponseDto(incident)).thenReturn(preview);

    ResponseEntity<List<IncidentPreviewResponseDto>> response =
        feedController.findAllInGivenRange(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsExactly(preview);
    verify(incidentUseCase).findAllInGivenRange(any(RadiusCommand.class));
  }

  @Test
  void findAllInGivenRangeReturnsBadRequestOnInvalidCoordinates() {
    RadiusRequestDto request = new RadiusRequestDto(10.0, 20.0, 500);
    when(incidentUseCase.findAllInGivenRange(any(RadiusCommand.class)))
        .thenThrow(new InvalidCoordinatesException("invalid"));

    ResponseEntity<List<IncidentPreviewResponseDto>> response =
        feedController.findAllInGivenRange(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNull();
  }

  private Incident sampleIncident() {
    return Incident.builder()
        .id(new IncidentId(1L))
        .userId(new UserId("uid-1"))
        .locationId(new LocationId(5L))
        .title("Road closure")
        .description("desc")
        .media(Set.of(new Media(1, "photo.png", "image/png")))
        .sentimentEngagement(SentimentEngagement.builder().likes(1).dislikes(0).build())
        .engagementStats(new EngagementStats(0, 0, 0))
        .build();
  }

  private IncidentPreviewResponseDto samplePreview() {
    return IncidentPreviewResponseDto.builder()
        .id(1L)
        .title("Road closure")
        .media(Set.of())
        .lat(10.0)
        .lon(20.0)
        .build();
  }
}
