package com.example.crud.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class FirebaseInitializer {

    @Value("${firebase.config}")
    private String firebaseConfigJson;

    @PostConstruct
    public void initialize() {
        try {
            System.out.println("FirebaseInitializer: Attempting to load firebase.config secret."); // New log
            String firebaseConfig = this.firebaseConfigJson;
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