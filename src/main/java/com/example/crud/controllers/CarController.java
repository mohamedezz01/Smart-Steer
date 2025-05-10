package com.example.crud.controllers;

import com.example.crud.dto.AIResponse;
import com.example.crud.dto.CarMessageDTO;
import com.example.crud.service.AIService;
import com.example.crud.service.CarService;
import com.example.crud.service.NotificationService;
import com.example.crud.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;


@RestController
@RequestMapping("/GP/car")
public class CarController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final CarService carService;
    private final AIService aiService;

    public CarController(UserService userService, NotificationService notificationService, CarService carService, AIService aiService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.carService = carService;
        this.aiService = aiService;
    }


    @PostMapping("/message")
    public ResponseEntity<String> saveCarMessage(@RequestBody CarMessageDTO carMessageDTO) {
        carService.saveMessage(carMessageDTO.getMessage());
        return ResponseEntity.ok("Message saved successfully.");
    }

    @PostMapping("/prediction")
    public ResponseEntity<AIResponse> handleAIprediction(@RequestBody AIResponse aiResponse) throws IOException {
        int predictedActionCode = aiResponse.getAction();
        System.out.println("Received AI prediction action code: " + predictedActionCode);
        return ResponseEntity.ok().build();
    }

}
