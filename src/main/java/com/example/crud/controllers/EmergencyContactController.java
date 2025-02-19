package com.example.crud.controllers;

import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import com.example.crud.service.EmergencyContactService;
import com.example.crud.service.UserService;
import com.example.crud.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
    private MessageSource messageSource;
    @Autowired
    public EmergencyContactController(EmergencyContactService emergencyContactService, UserService userService, JwtUtil jwtUtil,MessageSource messageSource) {
        this.emergencyContactService = emergencyContactService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.messageSource=messageSource;
    }

    //add a new emergency contact
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addEmergencyContact(
            @RequestBody EmergencyContact contact, @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "Accept-Language", required = false) String lang) {

        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        // Validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        //extract the token
        String token = authHeader.replace("Bearer ", "");

        //extract email/username from the token
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        boolean contactExists = emergencyContactService.existsByPhoneAndUser(contact.getPhone(), user);
        if (contactExists) {
            response.put("message", messageSource.getMessage("same.number",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        //set user association and save contact
        contact.setUser(user);
        EmergencyContact savedContact = emergencyContactService.addContact(contact);

        response.put("message", messageSource.getMessage("contact.added",null,locale));
        response.put("contact", savedContact);
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    //get all emergency contacts for logged-in user
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listEmergencyContacts(@RequestHeader("Authorization") String authHeader,
                                                                     @RequestHeader(value = "Accept-Language", required = false) String lang) {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        // Validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Extract the token and username
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        User user = userService.findByEmail(email);
        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found",null,locale));
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        List<EmergencyContact> contacts = emergencyContactService.getContactsByUserId(user.getId());

        if (contacts.isEmpty()) {
            response.put("message", messageSource.getMessage("empty.list",null,locale));
            response.put("contacts", Collections.emptyList());
            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        }

        response.put("contacts", contacts);
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    //udate an existing emergency contact
    @PutMapping("/update/{contactId}")
    public ResponseEntity<Map<String, Object>> updateEmergencyContact(
            @PathVariable int contactId, @RequestBody EmergencyContact updatedContact, @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "Accept-Language", required = false) String lang) {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        //validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Extract the token and username
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        User user = userService.findByEmail(email);
        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        //find and validate the existing contact
        EmergencyContact existingContact = emergencyContactService.findById(contactId);
        if (existingContact == null || existingContact.getUser().getId() != user.getId()) {
            response.put("message", messageSource.getMessage("contact.not.found",null,locale));
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Update fields and save the contact
        existingContact.setName(updatedContact.getName());
        existingContact.setPhone(updatedContact.getPhone());
        EmergencyContact savedContact = emergencyContactService.addContact(existingContact);

        response.put("message", messageSource.getMessage("contact.updated",null,locale));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    //delete an emergency contact
    @DeleteMapping("/delete/{contactId}")
    public ResponseEntity<Map<String, Object>> deleteEmergencyContact(
            @PathVariable int contactId, @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "Accept-Language", required = false) String lang) {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        // Validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Extract the token and username
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        User user = userService.findByEmail(email);
        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Find and validate the existing contact
        EmergencyContact existingContact = emergencyContactService.findById(contactId);
        if (existingContact == null || existingContact.getUser().getId() != user.getId()) {
            response.put("message", messageSource.getMessage("contact.not.found",null,locale));
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Delete the contact
        emergencyContactService.deleteContact(contactId);

        response.put("message", messageSource.getMessage("contact.deleted",null,locale));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
