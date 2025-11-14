package com.backend.adapter.outbound.repo.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.adapter.outbound.entity.MediaEntity;
import com.backend.adapter.outbound.repo.MediaPersistenceRepository;
import com.backend.domain.media.Media;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediaPersistenceTest {

  @Mock
  private MediaPersistenceRepository repository;

  @InjectMocks
  private MediaPersistence mediaPersistence;

  @Test
  void saveAllPersistsMediaAndReturnsMappedSet() throws Exception {
    Media media = new Media(100L, "photo.png", "image/png");
    MediaEntity entity = MediaEntity.builder()
        .id(42L)
        .key("photo.png")
        .size(100L)
        .contentType("image/png")
        .build();

    when(repository.saveAll(any(List.class))).thenReturn(List.of(entity));

    Set<Media> saved = mediaPersistence.saveAll(Set.of(media));

    assertThat(saved).containsExactly(media);
    verify(repository).saveAll(any(List.class));
  }

  @Test
  void saveAllReturnsEmptyWhenInputEmpty() throws Exception {
    Set<Media> saved = mediaPersistence.saveAll(Set.of());
    assertThat(saved).isEmpty();
  }

  @Test
  void deleteAllByKeysDelegatesToRepository() throws Exception {
    Set<String> keys = Set.of("file1", "file2");
    mediaPersistence.deleteAllByKeys(keys);
    verify(repository).deleteByKeyIn(keys);
  }

  @Test
  void deleteAllByKeysSkipsWhenEmpty() throws Exception {
    mediaPersistence.deleteAllByKeys(Set.of());
    verify(repository, never()).deleteByKeyIn(any());
  }

  @Test
  void findByKeyReturnsDomainMedia() throws Exception {
    MediaEntity entity = MediaEntity.builder()
        .id(5L)
        .key("photo.png")
        .contentType("image/png")
        .size(50L)
        .build();

    when(repository.findByKey("photo.png")).thenReturn(Optional.of(entity));

    Optional<Media> result = mediaPersistence.findByKey("photo.png");
    assertThat(result).contains(new Media(50L, "photo.png", "image/png"));
  }

  @Test
  void findByKeyReturnsEmptyWhenMissing() throws Exception {
    when(repository.findByKey("missing")).thenReturn(Optional.empty());

    Optional<Media> result = mediaPersistence.findByKey("missing");
    assertThat(result).isEmpty();
  }
}
