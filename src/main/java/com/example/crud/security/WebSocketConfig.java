package com.example.crud.security;

import com.example.crud.service.CustomWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CustomWebSocketHandler webSocketHandler;

    public WebSocketConfig(CustomWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Change this path to match your SecurityConfig
        registry.addHandler(webSocketHandler, "/GP/ws") // <--- CHANGED HERE
                .setAllowedOrigins("*"); // allow all during dev — restrict in prod
    }
}