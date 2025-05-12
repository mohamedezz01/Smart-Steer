package com.example.crud.controllers;

import com.example.crud.dao.SerialNumberRepository;
import com.example.crud.dto.AIResponse;
import com.example.crud.dto.CarMessageDTO;
import com.example.crud.dto.LastEmergencyUserHolder;
import com.example.crud.entity.SerialNumber;
import com.example.crud.entity.User;
import com.example.crud.service.*;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/GP/car")
public class CarController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final CarService carService;
    private final AIService aiService;
    private final SerialNumberRepository serialNumberRepository;
    private CustomWebSocketHandler webSocketHandler;

    public CarController(UserService userService, NotificationService notificationService, CarService carService, AIService aiService, SerialNumberRepository serialNumberRepository, CustomWebSocketHandler webSocketHandler) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.carService = carService;
        this.aiService = aiService;
        this.serialNumberRepository = serialNumberRepository;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/ultrasonic")//message previously
    public ResponseEntity<String> saveCarMessage( @RequestHeader("serialNumber") String serialNumber,@RequestBody CarMessageDTO dto) {

        Optional<SerialNumber> optionalSn = serialNumberRepository.findBySerialNumber(serialNumber);

        if (optionalSn.isEmpty() || optionalSn.get().getUser() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or unassigned serial number.");
        }

        User user = optionalSn.get().getUser();
        if ("0".equals(dto.getLeft()) || "0".equals(dto.getRight())) {
            LastEmergencyUserHolder.setUserId(user.getId());

            Map<String, String> data = new HashMap<>();
            data.put("type", "accident");

            try {
                notificationService.sendDataNotification(user.getFcmToken(), data);
                notificationService.sendEmergencyNotification(
                        user.getFcmToken(),
                        "🚨 EMERGENCY DETECTED 🚨",
                        "Sending assistance to your location now",
                        data
                );
            } catch (FirebaseMessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send notification.");
            }
        }

        return ResponseEntity.ok("Message processed.");
    }


    @PostMapping("/prediction")
    public ResponseEntity<Void> handleAIprediction(@RequestBody AIResponse aiResponse) {
        int predictedActionCode = aiResponse.getAction();
        System.out.println("Received AI prediction action code: " + predictedActionCode);

        if (predictedActionCode == 14) {
            try {
                webSocketHandler.broadcast("stop");
                System.out.println("STOP command broadcasted to cars.");
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.ok().build();
    }


}
