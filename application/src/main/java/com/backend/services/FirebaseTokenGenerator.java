package com.backend.services;

import com.google.auth.oauth2.GoogleCredentials;
import jakarta.validation.executable.ValidateOnExecution;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.FileInputStream;

@Component
public final class FirebaseTokenGenerator {

    private static final String SERVICE_ACCOUNT_PATH =
            "infra/src/main/resources/aroundly-5c094-120456bfee62.json";

    @Value("${firebase-api-key}")
    private static String apiKey;

    private FirebaseTokenGenerator() {

    }

    /**
     * Generates a Firebase ID token for the given UID.
     */
    public static String generateUserIdToken(String uid) {
        try {
            // Initialize Firebase admin once
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }

            // 1. Create custom token
            String customToken = FirebaseAuth.getInstance().createCustomToken(uid);

            // 2. Exchange it for ID token via REST API
            OkHttpClient client = new OkHttpClient();
            JSONObject body = new JSONObject();
            body.put("token", customToken);
            body.put("returnSecureToken", true);

            RequestBody requestBody = RequestBody.create(
                    body.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url("https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key=" + apiKey)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Token exchange failed: " + response.code() + " " + response.message());
                }
                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);
                return json.getString("idToken");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error generating Firebase ID token", e);
        }
    }

    public static void main(String[] args) {
        String idToken = generateUserIdToken("test-user");
        System.out.println("Firebase ID token:\n" + idToken);
    }
}
