package com.example.crud.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    // Handle messages sent to "/app/collision"
    @MessageMapping("/collision")
    @SendTo("/topic/collision-alerts")
    public String handleCollision(String message) {
        // Process the collision alert
        System.out.println("Collision detected: " + message);
        return "Collision Alert: " + message;
    }

    // Handle messages sent to "/app/sensor-data"
    @MessageMapping("/sensor-data")
    @SendTo("/topic/sensor-updates")
    public String handleSensorData(String data) {
        // Process the sensor data
        System.out.println("Received sensor data: " + data);
        return "Sensor Data: " + data;
    }
}