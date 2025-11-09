package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.MediaEntity;
import com.backend.domain.media.Media;

public final class MediaEntityMappers {

  public static Media toDomain(MediaEntity entity) {
    return new Media(
        entity.getSize(),
        entity.getKey(),
        entity.getContentType());
  }

  public static MediaEntity toEntity(Media media) {
    return MediaEntity.builder()
        .size(media.size())
        .key(media.filename())
        .contentType(media.contentType())
        .build();
  }

}
