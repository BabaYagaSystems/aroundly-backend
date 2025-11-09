package com.backend.adapter.outbound.repo;

import com.backend.adapter.outbound.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for accessing user data.
 */
@Repository
public interface UserPersistenceRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by their Firebase UID.
     *
     * @param firebaseUid the Firebase user ID
     * @return Optional containing the user if found
     */
    Optional<UserEntity> findByFirebaseUid(String firebaseUid);

    /**
     * Finds a user by their email address.
     *
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Checks if a user exists with the given Firebase UID.
     *
     * @param firebaseUid the Firebase user ID
     * @return true if user exists, false otherwise
     */
    boolean existsByFirebaseUid(String firebaseUid);
}