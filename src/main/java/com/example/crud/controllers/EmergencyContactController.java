package com.example.crud.controllers;

import com.example.crud.dao.EmergencyContactRepository;
import com.example.crud.dto.LocationDTO;
import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import com.example.crud.service.EmailServ;
import com.example.crud.service.EmergencyContactService;
import com.example.crud.service.NotificationService;
import com.example.crud.service.UserService;
import com.example.crud.util.JwtUtil;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/GP/emergency")
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    @Autowired
    private EmailServ emailService;
    @Autowired
    private EmergencyContactRepository emergencyContactRepository;
    private NotificationService notificationService;
    @Autowired
    public EmergencyContactController(EmergencyContactService emergencyContactService, UserService userService, JwtUtil jwtUtil, EmailServ emailService,EmergencyContactRepository emergencyContactRepository,NotificationService notificationService) {
        this.emergencyContactService = emergencyContactService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.emergencyContactRepository=emergencyContactRepository;
        this.notificationService=notificationService;
    }

    //add a new emergency contact
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addEmergencyContact(
            @RequestBody EmergencyContact contact, @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        // Validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        //extract the token
        String token = authHeader.replace("Bearer ", "");

        //extract email/username from the token
        String email = jwtUtil.extractEmail(token);

        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        boolean contactExists = emergencyContactService.existsByPhoneAndUser(contact.getPhone(), user);
        if (contactExists) {
            response.put("message", "A contact with the same phone number already exists");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
         if(Objects.equals(contact.getPhone(), user.getPhone())){
            response.put("message", "You can't use your profile number as a new contact. Please enter a different one");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        boolean emailExists=emergencyContactService.existsByEmailAndUser(contact.getEmail(),user);
        if (emailExists) {
            response.put("message", "A contact with the same email already exists");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if(Objects.equals(contact.getEmail(), user.getEmail())){
            response.put("message", "You can't use your email as a new contact. Please enter a different one");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        emergencyContactService.notifyUserIfEmailExists(
                contact.getEmail(),
                user.getFirstName(),
                user.getPhone()
        );
        contact.setUser(user);
        EmergencyContact savedContact = emergencyContactService.addContact(contact);
        response.put("message", "Emergency contact added successfully.");
        response.put("contact", Map.of(
                "id",savedContact.getId(),
                "name", savedContact.getName(),
                "phone", savedContact.getPhone(),
                "email",savedContact.getEmail()
        ));
        response.put("status", HttpStatus.OK.value());

        return ResponseEntity.ok(response);
    }

    //get all emergency contacts for logged-in user
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listEmergencyContacts(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        // Validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        User user = userService.findByEmail(email);
        if (user == null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        List<EmergencyContact> contacts = emergencyContactService.getContactsByUserId(user.getId());

        if (contacts.isEmpty()) {
            response.put("message", "No emergency contacts found.");
            response.put("contacts", Collections.emptyList());
            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        }

        List<Map<String, String>> filteredContacts = contacts.stream()
                .map(contact -> Map.of(
                        "id",String.valueOf(contact.getId()),
                        "name", contact.getName(),
                        "phone", contact.getPhone(),
                        "email", contact.getEmail()
                ))
                .toList();

        response.put("message", "Emergency contacts retrieved successfully.");
        response.put("contacts", filteredContacts);
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    //update an existing emergency contact

    @PutMapping("/update/{contactId}")
    public ResponseEntity<Map<String, Object>> updateEmergencyContact(
            @PathVariable int contactId, @RequestBody EmergencyContact updatedContact, @RequestHeader("Authorization") String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        EmergencyContact existingContact = emergencyContactService.findById(contactId);

        if (existingContact == null || existingContact.getUser().getId() != user.getId()) {
            response.put("message", "Emergency contact not found or access denied.");
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        boolean contactExists = emergencyContactService.existsByPhoneAndUser(updatedContact.getPhone(), user) &&
                !existingContact.getPhone().equals(updatedContact.getPhone());


        if (contactExists) {
            response.put("message", "A contact with the same phone number already exists");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if(Objects.equals(existingContact.getPhone(), user.getPhone())){
            response.put("message", "You can't use your profile number as a new contact. Please enter a different one");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean emailExists = emergencyContactService.existsByEmailAndUser(updatedContact.getEmail(), user) &&
                !existingContact.getEmail().equals(updatedContact.getEmail());

        if (emailExists) {
            response.put("message", "A contact with the same email already exists");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if(Objects.equals(existingContact.getEmail(), user.getEmail())){
            response.put("message", "You can't use your email as a new contact. Please enter a different one");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (!existingContact.getEmail().equals(updatedContact.getEmail())) {
            emergencyContactService.notifyUserIfEmailExists(
                    updatedContact.getPhone(),
                    user.getFirstName(),
                    user.getPhone()
            );
        }

        existingContact.setName(updatedContact.getName());
        existingContact.setPhone(updatedContact.getPhone());
        EmergencyContact savedContact = emergencyContactService.addContact(existingContact);
        response.put("message", "Emergency contact updated successfully");
        response.put("contact", Map.of(
                "id",savedContact.getId(),
                "name", savedContact.getName(),
                "phone", savedContact.getPhone(),
                "email",savedContact.getEmail()
        ));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    //delete an emergency contact
    @DeleteMapping("/delete/{contactId}")
    public ResponseEntity<Map<String, Object>> deleteEmergencyContact(
            @PathVariable int contactId, @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        // Validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Extract the token and username
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        User user = userService.findByEmail(email);
        if (user == null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Find and validate the existing contact
        EmergencyContact existingContact = emergencyContactService.findById(contactId);
        if (existingContact == null || existingContact.getUser().getId() != user.getId()) {
            response.put("message", "Emergency contact not found or access denied.");
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Delete the contact
        emergencyContactService.deleteContact(contactId);

        response.put("message", "Emergency contact deleted successfully.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/location")
    public ResponseEntity<String> handleEmergencyLocation(
            @RequestHeader("Authorization") String token,@RequestBody LocationDTO locationDTO) {
        String jwt = token.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(jwt);
        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
        }

        List<EmergencyContact> contacts = emergencyContactRepository.findByUser(user);
        String mapLink = "https://maps.google.com/?q=" + locationDTO.getLat() + "," + locationDTO.getLng();

        emailService.sendEmergencyEmails(user, contacts, mapLink);
        return ResponseEntity.ok("Location received and emails sent.");
    }
    @PostMapping("/alert")
    public ResponseEntity<String> sendEmergencyNotification(@RequestHeader("Authorization") String authHeader) throws FirebaseMessagingException {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null || user.getFcm_token() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found or FCM token missing.");
        }
        Map<String, String> data = new HashMap<>();
        data.put("type","accident");

        notificationService.sendEmergencyNotification(
                user.getFcm_token(),
                "🚨 EMERGENCY DETECTED 🚨",
                "Potential accident detected. Dispatching assistance to your location now."
        );

        return ResponseEntity.ok("Emergency notification sent.");
    }
}
