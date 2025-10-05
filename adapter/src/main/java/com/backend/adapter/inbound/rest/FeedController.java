package com.backend.adapter.inbound.rest;

import com.backend.adapter.inbound.dto.request.RadiusRequestDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.inbound.mapper.IncidentMapper;
import com.backend.adapter.inbound.mapper.LocationMapper;
import com.backend.adapter.inbound.rest.exception.incident.InvalidCoordinatesException;
import com.backend.adapter.outbound.factory.MediaPreviewFactory;
import com.backend.domain.happening.Incident;
import com.backend.port.inbound.IncidentUseCase;
import com.backend.port.inbound.commands.RadiusCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
@Slf4j
@Tag(name = "Feed", description = "Content feed endpoints for incidents and events")
@SecurityRequirement(name = "bearerAuth")
public class FeedController {

  private final LocationMapper locationMapper;
  private final IncidentUseCase incidentUseCase;
  private final IncidentMapper incidentMapper;
  private final MediaPreviewFactory mediaPreviewFactory;

  public FeedController(
      LocationMapper locationMapper,
      IncidentUseCase incidentUseCase,
      IncidentMapper incidentMapper,
      MediaPreviewFactory mediaPreviewFactory) {

    this.locationMapper = locationMapper;
    this.incidentUseCase = incidentUseCase;
    this.incidentMapper = incidentMapper;
    this.mediaPreviewFactory = mediaPreviewFactory;
  }


  @GetMapping
    @Operation(
            summary = "Get feed content",
            description = "Retrieves incidents and events for the user's personalized feed"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feed content"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<IncidentPreviewResponseDto>> findAllInGivenRange(
      @ModelAttribute @Valid RadiusRequestDto radiusRequestDto) {
      try {
        RadiusCommand radiusCommand = locationMapper.toRadiusCommand(radiusRequestDto);
        List<Incident> incidents = incidentUseCase.findAllInGivenRange(radiusCommand);
        List<IncidentPreviewResponseDto> responseDtos = incidents.stream()
            .map(incident -> {
              IncidentPreviewResponseDto dto = incidentMapper.toIncidentPreviewResponseDto(incident);
              return dto.toBuilder()
                  .media(mediaPreviewFactory.build(incident.media()))
                  .build();
            })
            .toList();

        return ResponseEntity.ok(responseDtos);
      } catch (InvalidCoordinatesException e) {
        log.warn("Invalid coordinates provided: {}", e.getMessage());
        return ResponseEntity.badRequest().build();
      }
    }
}
