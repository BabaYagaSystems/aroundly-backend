package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.domain.happening.Incident;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncidentEntityMapper {

    @Mapping(target = "timePosted", ignore = true)
    @Mapping(target = "range", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "happening", ignore = true)
    @Mapping(source = "engagementStats.confirms", target = "confirms")
    @Mapping(source = "engagementStats.denies", target = "denies")
    IncidentEntity toIncidentEntity(Incident incident);

    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "engagementStats", ignore = true)
    Incident toIncident(IncidentEntity incidentEntity);
}