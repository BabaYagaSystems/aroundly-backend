package com.backend.adapter.inbound.mapper;

import com.backend.adapter.inbound.dto.request.IncidentRequestDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.inbound.mapper.assembler.UploadMediaMapperAssembler;
import com.backend.domain.happening.Incident;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between domain {@link Incident}
 * entities and various DTOs.
 */
@Mapper(componentModel = "spring", uses = {UploadMediaMapperAssembler.class})
public interface IncidentMapper {

  /**
   * Maps an incoming request DTO to a command for creating a new incident.
   *
   * @param incidentRequestDto the request DTO with incident data
   * @return command object to create an incident
   */

  @Mapping(target = "actorId", ignore = true)
  @Mapping(target = "media", source = "files")
  CreateIncidentCommand toCreateIncidentCommand(IncidentRequestDto incidentRequestDto);

  /**
   * Maps a domain {@link Incident} to a detailed response DTO.
   *
   * Certain fields (location, actorUsername) are ignored here
   * and must be set later by the assembler.
   *
   * @param incident the domain incident entity
   * @return a detailed response DTO
   */
  @Mapping(target = "id", source = "id")
  @Mapping(target = "media", ignore = true)
  @Mapping(target = "like", source = "sentimentEngagement.likes")
  @Mapping(target = "dislike", source = "sentimentEngagement.dislikes")
  @Mapping(target = "deny", source = "engagementStats.denies")
  @Mapping(target = "consecutiveDenies", source = "engagementStats.consecutiveDenies")
  @Mapping(target = "confirm", source = "engagementStats.confirms")
  @Mapping(target = "lon", ignore = true)
  @Mapping(target = "lat", ignore = true)
  @Mapping(target = "address", ignore = true)
  @Mapping(target = "actorUsername", ignore = true)
  IncidentDetailedResponseDto toIncidentDetailedResponseDto(Incident incident);

  /**
   * Maps a domain {@link Incident} to a preview DTO
   * containing summarized incident details.
   *
   * @param incident the domain incident entity
   * @return a preview response DTO
   */
  @Mapping(target = "lon", ignore = true)
  @Mapping(target = "lat", ignore = true)
  IncidentPreviewResponseDto toIncidentPreviewResponseDto(Incident incident);

}
