package com.example.crud.service;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendEmergencyNotification(String fcmToken, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .putData("type", "emergency")
                .putData("title", title)
                .putData("body", body)
                .setToken(fcmToken)
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Notification sent. Response: " + response);
    }

}
