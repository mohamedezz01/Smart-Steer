package com.example.crud.service;

// ... other imports
import com.example.crud.dao.EmergencyContactRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.net.URI; // Keep this if you still want to log the URI or for future use
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger; // It's good practice to use a logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketHandler.class); // Add logger
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    // Remove JWTUtil, UserService if no longer needed for initial connection auth here
    // private final JwtUtil jwtUtil;
    // private final UserService userService;
    private final NotificationService notificationService; // Keep if used for other logic
    private final EmailServ emailService; // Keep if used for other logic
    private final EmergencyContactRepository emergencyContactRepository; // Keep if used for other logic

    // Adjust constructor if you remove jwtUtil and userService from here
    public CustomWebSocketHandler(/*JwtUtil jwtUtil, UserService userService,*/
            NotificationService notificationService,
            EmailServ emailService,
            EmergencyContactRepository emergencyContactRepository) {
        // this.jwtUtil = jwtUtil;
        // this.userService = userService;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.emergencyContactRepository = emergencyContactRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri(); // You can still get the URI if needed for logging or other purposes
        logger.info("WebSocket connection established. Session ID: {}, URI: {}", session.getId(), uri != null ? uri.toString() : "N/A");

        // No authentication check here. Simply add the session.
        sessions.add(session);
        // You can put an anonymous user or a generic marker in session attributes if needed
        // session.getAttributes().put("userType", "ANONYMOUS_WEBSOCKET_USER");

        System.out.println("WebSocket connected (anonymous): " + session.getId()); // Updated log
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Message received from session {}: {}", session.getId(), message.getPayload());

        Map<String, Object> payload = new ObjectMapper().readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        if ("action".equalsIgnoreCase(type)) {
            Integer actionValue = (Integer) payload.get("value"); // e.g., 14

            if (actionValue != null && actionValue == 14) {
                logger.info("Action 14 received. Broadcasting STOP to all sessions.");
                // Broadcast "STOP" to all connected clients (cars)
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage("STOP"));
                    }
                }
                // Sending STOP back to the originator might be redundant if they are part of 'sessions'
                // but can be kept if specific behavior is desired.
                // session.sendMessage(new TextMessage("STOP"));
            } else {
                logger.warn("Unknown action value for type 'action': {}", actionValue);
                session.sendMessage(new TextMessage("Unknown action value."));
            }
        } else {
            logger.warn("Unknown message type: {}", type);
            session.sendMessage(new TextMessage("Unknown message type."));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("WebSocket closed: Session ID: {}, Status: {}", session.getId(), status);
    }

    public void broadcast(String message) throws IOException {
        logger.info("Broadcasting message to {} sessions: {}", sessions.size(), message);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(message));
            }
        }
    }

    public void closeAllConnections() throws IOException {
        logger.info("Closing all WebSocket connections by admin request.");
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.close(CloseStatus.NORMAL.withReason("Closed by admin"));
            }
        }
        sessions.clear();
    }
}