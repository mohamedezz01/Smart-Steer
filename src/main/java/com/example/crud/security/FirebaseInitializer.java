package com.example.crud.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseInitializer { // Renamed from FirebaseConfig, which is fine

    // Reads the value from the environment variable FIREBASE_CREDENTIALS_PATH
    // (which Spring maps to the firebase.credentials.path property)
    // Uses E:\firebase-admin-sdk.json as a default if the env var isn't set (e.g., locally)
    @Value("${firebase.credentials.path:E:\\firebase-admin-sdk.json}")
    private String firebaseCredentialsPath;

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        System.out.println("Initializing Firebase App from path: " + firebaseCredentialsPath);

        // Uses FileInputStream to read the file from the path obtained above
        InputStream serviceAccount = new FileInputStream(firebaseCredentialsPath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("Firebase App [DEFAULT] initialized successfully.");
            return FirebaseApp.initializeApp(options);
        } else {
            System.out.println("Firebase App [DEFAULT] already exists.");
            return FirebaseApp.getInstance();
        }
    }
}