package com.example.crud.service;

import com.example.crud.entity.User;
import com.example.crud.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public CustomWebSocketHandler(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        String query = uri != null ? uri.getQuery() : null;

        if (query == null || !query.startsWith("token=")) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing or invalid token"));
            return;
        }

        String token = query.substring(6); // remove "token="

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
        System.out.println("📩 Message: " + message.getPayload());

        // Echo or handle based on type
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage("Echo: " + message.getPayload()));
            }
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
