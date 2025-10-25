package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.MediaEntity;
import com.backend.domain.media.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between {@link MediaEntity} and {@link Media}.
 *
 * Uses MapStruct to generate type-safe mappings for persistence and domain models.
 */
@Mapper(componentModel = "spring")
public interface MediaEntityMapper {

  /**
   * Converts a {@link MediaEntity} into its corresponding domain object.
   *
   * @param entity the persistence entity
   * @return the mapped domain object
   */
  @Mapping(target = "filename", source = "key")
  Media toDomain(MediaEntity entity);

  /**
   * Converts a {@link Media} domain object into its persistence entity.
   *
   * @param media the domain object
   * @return the mapped persistence entity
   */
  @Mapping(target = "incidentEntity", ignore = true)
  @Mapping(target = "key", source = "filename")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  MediaEntity toEntity(Media media);
}
