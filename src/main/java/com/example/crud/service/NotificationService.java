package com.example.crud.service;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {


    public void sendEmergencyNotification(String fcmToken, Map<String, String> data) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(fcmToken)
                .putAllData(data)
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Notification sent. Response: " + response);
    }

//    public void sendEmergencyNotification(String fcmToken, String title, String body) throws FirebaseMessagingException {
//        Map<String, String> data = new HashMap<>();
//        data.put("type", "accident");
//
//        Message message = Message.builder()
//                .setToken(fcmToken)
//                .putAllData(data)
//                .setNotification(Notification.builder()
//                        .setTitle(title)
//                        .setBody(body)
//                        .build()
//                )
//                .build();
//        String response = FirebaseMessaging.getInstance().send(message);
//        System.out.println("Notification sent. Response: " + response);
//    }

}
