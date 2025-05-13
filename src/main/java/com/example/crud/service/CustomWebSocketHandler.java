

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
        User user = null; // Initialize user to null

        if (query != null && query.startsWith("token=")) {
            // --- Handle Token Authentication ---
            String token = query.substring(6);
            try {
                String email = jwtUtil.extractEmail(token);
                user = userService.findByEmail(email); // Attempt to find user

                if (user != null) {
                    // Authentication successful
                    session.getAttributes().put("user", user);
                    System.out.println("WebSocket connected (authenticated): " + email + ", SessionID: " + session.getId());
                } else {
                    // Token was valid, but user doesn't exist in DB
                    System.out.println("Authentication failed: User not found for email " + email + ", SessionID: " + session.getId());
                    // You might still add the session if anonymous connections are allowed
                    // or close it if strict authentication is required for ANY interaction
                    // session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User not found"));
                }
            } catch (Exception e) {
                // Token was invalid (expired, malformed, etc.)
                System.out.println("Authentication failed: Invalid token. " + e.getMessage() + ", SessionID: " + session.getId());
                // Again, decide whether to add/keep anonymous or close
                // session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token: " + e.getMessage()));
            }
        } else {
            // --- Handle Anonymous Connection ---
            System.out.println("WebSocket connected (anonymous), SessionID: " + session.getId());
            // No token provided or query string is malformed
        }

        // --- Add the session to the set for broadcasting, regardless of authentication status ---
        // Only add if you haven't explicitly closed it above
        if (session.isOpen()) {
            sessions.add(session);
            System.out.println("Session added to broadcast set: " + session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Message: " + message.getPayload());

        Map<String, Object> payload = new ObjectMapper().readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        if ("action".equalsIgnoreCase(type)) {
            Integer actionValue = (Integer) payload.get("value"); // e.g., 14

            if (actionValue != null && actionValue == 14) {
                // Broadcast "STOP" to all connected clients (cars)
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage("stop"));
                    }
                }
                session.sendMessage(new TextMessage("stop"));
            }
            else {
                session.sendMessage(new TextMessage("Unknown message type."));
            }

        }}


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
