package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.MediaEntity;
import com.backend.adapter.outbound.mapper.MediaEntityMappers;
import com.backend.adapter.outbound.repo.MediaPersistenceRepository;
import com.backend.domain.media.Media;
import com.backend.port.outbound.repo.MediaRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * JPA-based implementation of {@link MediaRepository}.
 */
@Repository
@RequiredArgsConstructor
public class MediaPersistence implements MediaRepository {

  private final MediaPersistenceRepository repository;

  /**
   * Saves a set of media objects to the database.
   *
   * @param media the set of domain media objects to save
   * @return the saved set of media objects with updated state from persistence
   * @throws Exception if persistence fails
   */
  @Override
  @Transactional
  public Set<Media> saveAll(Set<Media> media) {
    if (media == null || media.isEmpty()) return Set.of();

    List<MediaEntity> entities = media.stream()
      .map(MediaEntityMappers::toEntity)
      .collect(Collectors.toList());

    List<MediaEntity> saved = repository.saveAll(entities);

    return saved.stream()
      .map(MediaEntityMappers::toDomain)
      .collect(Collectors.toSet());
  }

  /**
   * Deletes all media records matching the given keys.
   *
   * @param keys the set of media keys to delete
   * @throws Exception if persistence fails
   */
  @Override
  @Transactional
  public void deleteAllByKeys(Set<String> keys) throws Exception {
    if (keys == null || keys.isEmpty()) return;
    repository.deleteByKeyIn(keys);
  }

  /**
   * Finds a media object by its key.
   *
   * @param key the unique key of the media
   * @return an {@link Optional} containing the media if found, otherwise empty
   * @throws Exception if persistence fails
   */
  @Override
  @Transactional
  public Optional<Media> findByKey(String key) throws Exception {
    if (key == null || key.isEmpty()) return Optional.empty();

    return repository.findByKey(key).map(MediaEntityMappers::toDomain);
  }
}
