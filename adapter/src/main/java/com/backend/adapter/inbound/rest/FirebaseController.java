package com.backend.adapter.inbound.rest;


import com.backend.adapter.outbound.entity.UserEntity;
import com.backend.adapter.outbound.repo.UserPersistenceRepository;
import com.backend.domain.actor.User;
import com.backend.port.inbound.UserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/firebase")
@RequiredArgsConstructor
@Slf4j
public class FirebaseController {

    private final UserUseCase userUseCase;
    private final UserPersistenceRepository userPersistenceRepository;

    /**
     * Test endpoint - checks if user is authenticated and in database
     *
     * Test with:
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/v1/firebase/test
     */
    @GetMapping("/test")
    public ResponseEntity<TestResponse> testAuth() {
        Optional<User> firebaseUser = userUseCase.getUser();

        if (firebaseUser.isEmpty()) {
            return ResponseEntity.ok(new TestResponse(
                    false,
                    "No authentication",
                    null,
                    null
            ));
        }

        User user = firebaseUser.get();
        Optional<UserEntity> dbUser = userPersistenceRepository.findByFirebaseUid(user.uid().value());

        return ResponseEntity.ok(new TestResponse(
                true,
                "User authenticated and synced",
                user,
                dbUser.orElse(null)
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return userUseCase.getUser()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    record TestResponse(
            boolean authenticated,
            String message,
            User firebaseUser,
            UserEntity databaseUser
    ) {}
}