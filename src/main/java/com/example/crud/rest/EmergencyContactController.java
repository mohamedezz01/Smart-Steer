package com.example.crud.rest;

import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import com.example.crud.service.EmergencyContactService;
import com.example.crud.service.UserService;
import com.example.crud.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/GP/emergency")
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    @Autowired
    public EmergencyContactController(EmergencyContactService emergencyContactService, UserService userService, JwtUtil jwtUtil) {
        this.emergencyContactService = emergencyContactService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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

        // Extract the token
        String token = authHeader.replace("Bearer ", "");

        // Extract email/username from the token
        String email = jwtUtil.extractUsername(token); // Ensure your JWT util has this method

        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Set user association and save contact
        contact.setUser(user);
        EmergencyContact savedContact = emergencyContactService.addContact(contact);

        response.put("message", "Emergency contact added successfully.");
        response.put("contact", savedContact);
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

        // Extract the token and username
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        User user = userService.findByEmail(email);
        if (user == null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        List<EmergencyContact> contacts = emergencyContactService.getContactsByUserId(user.getId());
        response.put("message", "Emergency contacts retrieved successfully.");
        response.put("contacts", contacts);
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    //udate an existing emergency contact
    @PutMapping("/update/{contactId}")
    public ResponseEntity<Map<String, Object>> updateEmergencyContact(
            @PathVariable int contactId, @RequestBody EmergencyContact updatedContact, @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        // Validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Extract the token and username
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

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

        // Update fields and save the contact
        existingContact.setName(updatedContact.getName());
        existingContact.setPhone(updatedContact.getPhone());
        EmergencyContact savedContact = emergencyContactService.addContact(existingContact);

        response.put("message", "Emergency contact updated successfully.");
        response.put("contact", savedContact);
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
        String email = jwtUtil.extractUsername(token);

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
}
