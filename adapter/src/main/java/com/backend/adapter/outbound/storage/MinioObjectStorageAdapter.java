package com.backend.adapter.outbound.storage;

import com.backend.domain.media.Media;
import com.backend.port.inbound.commands.UploadMediaCommand;
import com.backend.port.outbound.storage.ObjectStoragePort;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * MinIO adapter implementing {@link ObjectStoragePort}.
 */
@Repository
@RequiredArgsConstructor
public class MinioObjectStorageAdapter implements ObjectStoragePort {

  private final MinioClient minioClient;
  private final String bucket;

  /**
   * Uploads all media, creating the bucket if needed.
   */
  @Override
  public Set<Media> uploadAll(Set<UploadMediaCommand> uploads) throws Exception {
    if (uploads == null || uploads.isEmpty()) return Set.of();

    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()))
      minioClient.makeBucket(MakeBucketArgs.builder()
          .bucket(bucket)
          .build());

    Set<Media> out = new HashSet<>();

    for (UploadMediaCommand upload : uploads) {
      String key = uuidPlusName(upload.filename());

      try (var in = upload.stream()) {
        minioClient.putObject(
            io.minio.PutObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .stream(in, -1, 10485760)
                .contentType(upload.contentType())
                .build());
        out.add(new Media(upload.size(), key, upload.contentType()));
      }
    }
    return out;
  }

  /**
   * Deletes objects by keys, fails if any error occurs.
   */
  @Override
  public void deleteAllByKeys(Set<String> keys) throws Exception {
    if (keys == null || keys.isEmpty()) return;

    List<DeleteObject> deleteObjects = keys.stream().map(DeleteObject::new).toList();
    Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
      .bucket(bucket)
      .objects(deleteObjects)
      .build());

    for (Result<DeleteError> result : results) {
      DeleteError error = result.get();
      throw new RuntimeException("Error in deleting object " + error.objectName() + "; " + error.message());
    }
  }

  /**
   * Creates a presigned GET URL with max 7 days expiry.
   */
  @Override
  public String presignGet(String key, Duration ttl) throws Exception {
    int seconds = (int) Math.min(ttl.toSeconds(), 7 * 24 * 3600);
    if (seconds <= 0) seconds = 60;

    return minioClient.getPresignedObjectUrl(
      io.minio.GetPresignedObjectUrlArgs.builder()
        .method(Method.GET)
        .bucket(bucket)
        .object(key)
        .expiry(seconds)
        .build());
  }

  /**
   * Creates a presigned PUT URL with max 7 days expiry.
   */
  @Override
  public String presignPut(String key, Duration ttl) throws Exception {
    int seconds = (int) Math.min(ttl.toSeconds(), 7 * 24 * 3600);

    return minioClient.getPresignedObjectUrl(
        io.minio.GetPresignedObjectUrlArgs.builder()
            .method(Method.PUT)
            .bucket(bucket)
            .object(key)
            .expiry(seconds)
            .build());
  }

  private static String uuidPlusName(String name){
    String safe = (name==null||name.isBlank())
        ? "file"
        : name.replaceAll("[^a-zA-Z0-9._-]", "_");

    return java.util.UUID.randomUUID() + "-" + safe;
  }
}
