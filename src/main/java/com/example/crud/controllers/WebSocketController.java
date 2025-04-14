package com.example.crud.controllers;

import com.example.crud.service.CustomWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/GP/ws")
public class WebSocketController {

    private final CustomWebSocketHandler handler;

    public WebSocketController(CustomWebSocketHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody String msg) throws IOException {
        handler.broadcast(msg);
        return ResponseEntity.ok("Broadcasted to all sessions.");
    }

    @PostMapping("/closeAll")
    public ResponseEntity<String> closeAll() throws IOException {
        handler.closeAllConnections();
        return ResponseEntity.ok("All WebSocket connections closed.");
    }
}
