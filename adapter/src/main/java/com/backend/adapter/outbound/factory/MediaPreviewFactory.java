package com.backend.adapter.outbound.factory;

import com.backend.adapter.inbound.dto.media.MediaDto;
import com.backend.adapter.inbound.mapper.MediaMapper;
import com.backend.domain.media.Media;
import com.backend.port.outbound.storage.ObjectStoragePort;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MediaPreviewFactory {

  private final ObjectStoragePort objectStoragePort;

  private static final Duration MEDIA_TTL = Duration.ofMinutes(10);
  private final MediaMapper mediaMapper;

  public Set<MediaDto> build(Set<Media> media) {
    if (media == null || media.isEmpty()) return Set.of();

    return media.stream()
      .map(this::toMediaDto)
      .collect(Collectors.toSet());
  }

  private MediaDto toMediaDto(Media media) {
    try {
      String url = objectStoragePort.presignGet(media.filename(), MEDIA_TTL);
      return new MediaDto(url);

    } catch (Exception e) {
      throw new IllegalStateException("Failed to presign " + media.filename(), e);
    }
  }

}
