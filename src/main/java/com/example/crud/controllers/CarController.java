package com.example.crud.controllers;

import com.example.crud.dto.CarMessageDTO;
import com.example.crud.dto.EmergencyDTO;
import com.example.crud.dto.LastEmergencyUserHolder;
import com.example.crud.entity.User;
import com.example.crud.service.CarService;
import com.example.crud.service.NotificationService;
import com.example.crud.service.UserService;
import com.example.crud.util.JwtUtil;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/GP/car")
public class CarController {

    private final UserService userService;
  //  private final JwtUtil jwtUtil;
    private final NotificationService notificationService;
    private final CarService carService;

    public CarController(UserService userService,  NotificationService notificationService, CarService carService) {
        this.userService = userService;
   this.notificationService = notificationService;
        this.carService = carService;
    }

//    @PostMapping("/emergency")
//    public ResponseEntity<String> receiveEmergencyAlert(
//            @RequestHeader("Authorization") String token,
//            @RequestBody EmergencyDTO emergencyDTO) throws FirebaseMessagingException {
//
//        String jwt = token.replace("Bearer ", "");
//        String email = jwtUtil.extractEmail(jwt);
//        User user = userService.findByEmail(email);
//
//        if (user == null || user.getFcm_token() == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or FCM token missing.");
//        }
//
//        if ("accident".equalsIgnoreCase(emergencyDTO.getType())) {
//            notificationService.sendEmergencyNotification(
//                    user.getFcm_token(),
//                    "🚨 Emergency Detected!",
//                    "We detected a possible accident. Sending help!"
//            );
//
//            return ResponseEntity.ok("Emergency notification sent.");
//        }
//
//        return ResponseEntity.badRequest().body("Unknown emergency type.");
//    }

    @PostMapping("/message")
    public ResponseEntity<String> saveCarMessage(@RequestBody CarMessageDTO carMessageDTO) {
        carService.saveMessage(carMessageDTO.getMessage());
        return ResponseEntity.ok("Message saved successfully.");
    }


//    @PostMapping("/message")
//    public ResponseEntity<String> saveCarMessage(@RequestBody CarMessageDTO carMessageDTO) throws FirebaseMessagingException {
//        carService.saveMessage(carMessageDTO.getMessage());
//
//        // If the message is accident-related, send an alert
//        if ("accident".equalsIgnoreCase(carMessageDTO.getMessage())) {
//            User user = userService.findByDeviceId(carMessageDTO.getDeviceId());
//
//            if (user != null && user.getFcmToken() != null) {
//                Map<String, String> data = Map.of("type", "accident");
//
//                // Set user for use in /location if needed
//                LastEmergencyUserHolder.setUserId(user.getId());
//
//                notificationService.sendDataNotification(user.getFcmToken(), data);
//
//                notificationService.sendEmergencyNotification(
//                        user.getFcmToken(),
//                        "🚨 EMERGENCY DETECTED 🚨",
//                        "Sending assistance to your location now",
//                        data
//                );
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body("User not found or missing FCM token for deviceId: " + carMessageDTO.getDeviceId());
//            }
//        }
//
//        return ResponseEntity.ok("Message saved successfully.");
//    }



//    @PostMapping("/location")
//    public ResponseEntity<String> receiveLocation(@RequestBody LocationDTO locationDTO) {
//        // Handle location
//        System.out.println(locationDTO);
//        return ResponseEntity.ok("Location updated");
//    }

//    @PostMapping("/image")
//    public ResponseEntity<String> receiveImage(@RequestBody ImageDTO imageDTO) {
//        // Handle image
//        System.out.println("Image received with size: " + imageDTO.getBase64Image().length());
//        return ResponseEntity.ok("Image received");
//    }

//    @PostMapping("/sensorData")
//    public ResponseEntity<String> receiveSensorData(@RequestBody SensorDataDTO sensorDataDTO) {
//
//        System.out.println(sensorDataDTO);
//        return ResponseEntity.ok("Sensor data received");
//    }

//    @PostMapping("/emergency")
//    public ResponseEntity<String> receiveEmergency(@RequestBody EmergencyDTO emergencyDTO) {
//
//        return ResponseEntity.ok("Emergency alert received");
//    }

}
