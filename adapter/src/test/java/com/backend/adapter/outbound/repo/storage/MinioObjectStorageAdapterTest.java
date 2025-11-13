package com.backend.adapter.outbound.repo.storage;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.backend.adapter.outbound.storage.MinioObjectStorageAdapter;
import com.backend.domain.media.Media;
import com.backend.port.inbound.commands.UploadMediaCommand;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinioObjectStorageAdapterTest {

  private MinioClient minioClient;
  private final static String BUCKET = "test-bucket";
  private MinioObjectStorageAdapter adapter;

  @BeforeEach
  void setUp() {
    minioClient = mock(MinioClient.class);
    adapter = new MinioObjectStorageAdapter(minioClient, BUCKET);
  }

  @Test
  void testUploadEmptySet() throws Exception {
    assertTrue(adapter.uploadAll(null).isEmpty());
    assertTrue(adapter.uploadAll(Collections.emptySet()).isEmpty());
    verifyNoInteractions(minioClient);
  }

  @Test
  void testUploadMediaSet() throws Exception {
    when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

    UploadMediaCommand mediaCommand = new UploadMediaCommand(
        new ByteArrayInputStream(new byte[0]),
        "file.png",
        4L,
        "image/plain"
    );
    Set<Media> results = adapter.uploadAll(Set.of(mediaCommand));

    assertEquals(1, results.size());
    verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
  }

  @Test
  void testDeleteAllByKeys() throws Exception {
    String key = "key1";
    Set<String> keys = Set.of(key);

    @SuppressWarnings("unchecked")
    Result<DeleteError> result = mock(Result.class);
    Iterable<Result<DeleteError>> results = List.of();

    when(minioClient.removeObjects(any(RemoveObjectsArgs.class))).thenReturn(results);

    assertDoesNotThrow(() -> adapter.deleteAllByKeys(keys));
    verify(minioClient, times(1)).removeObjects(any(RemoveObjectsArgs.class));
  }

  @Test
  void testPresignGet() throws Exception {
    String key = "file.png";
    Duration ttl = Duration.ofMinutes(10);
    String url = "http://example.com/file.png";

    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(url);

    String result = adapter.presignGet(key, ttl);

    assertEquals(url, result);
    verify(minioClient, times(1))
      .getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
  }

  @Test
  void testPresignPut() throws Exception {
    String key = "file.png";
    Duration ttl = Duration.ofMinutes(10);
    String url = "http://example.com/file.png";

    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(url);

    String result = adapter.presignPut(key, ttl);

    assertEquals(url, result);
    verify(minioClient, times(1))
      .getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
  }

}
