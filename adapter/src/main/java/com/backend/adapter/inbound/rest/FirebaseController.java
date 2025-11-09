package com.backend.adapter.inbound.rest;


import com.backend.adapter.outbound.entity.UserEntity;
import com.backend.adapter.outbound.repo.UserPersistenceRepository;
import com.backend.domain.actor.FirebaseUserInfo;
import com.backend.services.AuthenticatedUserService;
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

    private final AuthenticatedUserService authenticatedUserService;
    private final UserPersistenceRepository userPersistenceRepository;

    /**
     * Test endpoint - checks if user is authenticated and in database
     *
     * Test with:
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/v1/firebase/test
     */
    @GetMapping("/test")
    public ResponseEntity<TestResponse> testAuth() {
        Optional<FirebaseUserInfo> firebaseUser = authenticatedUserService.getCurrentUser();

        if (firebaseUser.isEmpty()) {
            return ResponseEntity.ok(new TestResponse(
                    false,
                    "No authentication",
                    null,
                    null
            ));
        }

        FirebaseUserInfo user = firebaseUser.get();
        Optional<UserEntity> dbUser = userPersistenceRepository.findByFirebaseUid(user.uid());

        return ResponseEntity.ok(new TestResponse(
                true,
                "User authenticated and synced",
                user,
                dbUser.orElse(null)
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<FirebaseUserInfo> getCurrentUser() {
        return authenticatedUserService.getCurrentUser()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    record TestResponse(
            boolean authenticated,
            String message,
            FirebaseUserInfo firebaseUser,
            UserEntity databaseUser
    ) {}
}