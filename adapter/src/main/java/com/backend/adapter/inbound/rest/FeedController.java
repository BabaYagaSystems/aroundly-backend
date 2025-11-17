package com.backend.adapter.inbound.rest;

import static com.backend.adapter.inbound.mapper.LocationMapper.toRadiusCommand;

import com.backend.adapter.inbound.dto.request.RadiusRequestDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.inbound.mapper.IncidentResponseMapper;
import com.backend.domain.happening.Incident;
import com.backend.port.inbound.IncidentUseCase;
import com.backend.port.inbound.commands.RadiusCommand;
import com.backend.services.exceptions.InvalidCoordinatesException;
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

  private final IncidentUseCase incidentUseCase;
  private final IncidentResponseMapper incidentResponseMapper;

  public FeedController(
      IncidentUseCase incidentUseCase,
      IncidentResponseMapper incidentResponseMapper) {

    this.incidentUseCase = incidentUseCase;
    this.incidentResponseMapper = incidentResponseMapper;
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
        RadiusCommand radiusCommand = toRadiusCommand(radiusRequestDto);
        List<Incident> incidents = incidentUseCase.findAllInGivenRange(radiusCommand);
        List<IncidentPreviewResponseDto> responseDtos = incidents.stream()
            .map(incidentResponseMapper::toIncidentPreviewResponseDto)
            .toList();

        return ResponseEntity.ok(responseDtos);
      } catch (InvalidCoordinatesException e) {
        log.warn("Invalid coordinates provided: {}", e.getMessage());
        return ResponseEntity.badRequest().build();
      }
    }
}
