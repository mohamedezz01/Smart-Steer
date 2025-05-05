package com.example.crud.service;

import com.example.crud.dao.EmergencyContactRepository;
import com.example.crud.dto.LocationDTO;
import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import com.example.crud.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final NotificationService notificationService;
    @Autowired
    private EmailServ emailService;
    private EmergencyContactRepository emergencyContactRepository;

    public CustomWebSocketHandler(JwtUtil jwtUtil, UserService userService, NotificationService notificationService, EmailServ emailService,EmergencyContactRepository emergencyContactRepository) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.emergencyContactRepository=emergencyContactRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        String query = uri != null ? uri.getQuery() : null;

        if (query == null || !query.startsWith("token=")) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing or invalid token"));
            return;
        }

        String token = query.substring(6);

        try {
            String email = jwtUtil.extractEmail(token);
            User user = userService.findByEmail(email);

            if (user == null) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid user"));
                return;
            }

            System.out.println("WebSocket connected: " + email);
            session.getAttributes().put("user", user);
            sessions.add(session);

        } catch (Exception e) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Message: " + message.getPayload());

        Map<String, Object> payload = new ObjectMapper().readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        User user = (User) session.getAttributes().get("user");

        if ("fcm_token".equalsIgnoreCase(type)) {
            String token = (String) payload.get("token");
            if (user != null && token != null) {
                user.setFcmToken(token);
                userService.save(user);
                session.sendMessage(new TextMessage("FCM token saved."));
                System.out.println("FCM token saved for user: " + user.getEmail());
            }
//        } else if ("accident".equalsIgnoreCase(type)) {
//            if (user != null && user.getFcm_token() != null) {
//                notificationService.sendEmergencyNotification(
//                        user.getFcm_token(),
//                        "🚨 Emergency Detected!",
//                        "We detected a possible accident. Sending help!"
//                );
//                session.sendMessage(new TextMessage("Emergency notification sent."));
//            }
        } else if ("location".equalsIgnoreCase(type)) {
            Double lat = (Double) payload.get("lat");
            Double lng = (Double) payload.get("lng");

            if (user != null) {
                List<EmergencyContact> contacts = emergencyContactRepository.findByUser(user);
                String googleMapsLink = "https://maps.google.com/?q=" + lat + "," + lng;
                emailService.sendEmergencyEmails(user, contacts, googleMapsLink);
                session.sendMessage(new TextMessage("Location shared with emergency contacts."));
            }
        } else {
            session.sendMessage(new TextMessage("Unknown message type."));
        }

    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket closed: " + session.getId());
    }

    public void broadcast(String message) throws IOException, IOException {
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(message));
            }
        }
    }

    public void closeAllConnections() throws IOException {
        for (WebSocketSession s : sessions) {
            s.close(CloseStatus.NORMAL.withReason("Closed by admin"));
        }
        sessions.clear();
    }
}
