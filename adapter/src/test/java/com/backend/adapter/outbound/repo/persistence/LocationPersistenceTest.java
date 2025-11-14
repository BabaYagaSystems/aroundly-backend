package com.backend.adapter.outbound.repo.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.repo.LocationPersistenceRepository;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationPersistenceTest {

  @Mock
  private LocationPersistenceRepository locationPersistenceRepository;

  @InjectMocks
  private LocationPersistence locationPersistence;

  @Test
  void saveMapsAndPersistsLocation() {
    Location location = new Location(new LocationId(0), 20.0, 10.0, "Main St");

    when(locationPersistenceRepository.save(any(LocationEntity.class)))
        .thenAnswer(invocation -> {
          LocationEntity entity = invocation.getArgument(0);
          entity.setId(42L);
          return entity;
        });

    Location saved = locationPersistence.save(location);

    assertThat(saved.id().value()).isEqualTo(42L);
    assertThat(saved.longitude()).isEqualTo(20.0);
    assertThat(saved.latitude()).isEqualTo(10.0);
    verify(locationPersistenceRepository).save(any(LocationEntity.class));
  }

  @Test
  void findByIdReturnsMappedLocation() {
    LocationEntity entity = LocationEntity.builder()
        .id(5L)
        .lat(11.0)
        .lng(22.0)
        .addressText("Address")
        .build();

    when(locationPersistenceRepository.findById(5L))
        .thenReturn(Optional.of(entity));

    Location location = locationPersistence.findById(5L);

    assertThat(location.id().value()).isEqualTo(5L);
    assertThat(location.latitude()).isEqualTo(11.0);
    assertThat(location.longitude()).isEqualTo(22.0);
  }

  @Test
  void findByIdThrowsWhenMissing() {
    when(locationPersistenceRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(IllegalStateException.class, () -> locationPersistence.findById(99L));
  }

  @Test
  void findByCoordinateReturnsOptionalLocation() {
    LocationEntity entity = LocationEntity.builder()
        .id(7L)
        .lat(33.3)
        .lng(44.4)
        .addressText("Somewhere")
        .build();

    when(locationPersistenceRepository.findByLatAndLng(33.3, 44.4))
        .thenReturn(Optional.of(entity));

    Optional<Location> result = locationPersistence.findByCoordinate(33.3, 44.4);

    assertThat(result).isPresent();
    assertThat(result.get().address()).isEqualTo("Somewhere");
  }

  @Test
  void deleteByIdDelegatesToRepository() {
    locationPersistence.deleteById(8L);
    verify(locationPersistenceRepository).deleteById(8L);
  }
}
