package com.example.crud.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class FirebaseInitializer {

    @PostConstruct
    public void initialize() {
        try {
            String firebaseConfig = System.getenv("FIREBASE_CONFIG");

            if (firebaseConfig != null) {
                firebaseConfig = firebaseConfig.replace("\\n", "\n");

                InputStream serviceAccount = new ByteArrayInputStream(firebaseConfig.getBytes());

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    System.out.println("Firebase initialized.");
                } else {
                    System.out.println("Firebase already initialized.");
                }
            } else {
                System.out.println("FIREBASE_CONFIG not found. Firebase not initialized.");
            }

        } catch (Exception e) {
            System.out.println(" Failed to initialize Firebase: " + e.getMessage());
        }
    }
}