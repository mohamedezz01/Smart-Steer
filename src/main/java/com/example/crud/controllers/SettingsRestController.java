package com.example.crud.controllers;

import com.example.crud.dto.ChangeEmailRequest;
import com.example.crud.dto.ChangePasswordRequest;
import com.example.crud.dto.DeleteAccountRequest;
import com.example.crud.entity.User;
import com.example.crud.service.AuthorityService;
import com.example.crud.service.EmailService;
import com.example.crud.service.UserService;
import com.example.crud.util.JwtUtil;
import com.example.crud.util.VerificationUtil;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/GP/settings")
public class SettingsRestController {

    private UserService userService;
    private AuthorityService authorityService;
    private EmailService emailService;
    private VerificationUtil verficationUtil;
    private JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public SettingsRestController(UserService theUserService, AuthorityService authorityService, EmailService emailService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = theUserService;
        this.authorityService = authorityService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil=jwtUtil;
    }

//
//    @PatchMapping("/updateInfo")
//    public ResponseEntity<Map<String, Object>> updateUserFields(
//            @RequestParam(required = false) String firstName,
//            @RequestParam(required = false) String lastName,
//            @RequestParam(required = false) String phone,
//            @RequestParam(required = false) Date dob,
//            @RequestHeader("Authorization") String authHeader
//    ) {
//        Map<String, Object> response = new HashMap<>();
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            response.put("message", "Authorization header missing or invalid.");
//            response.put("status", HttpStatus.UNAUTHORIZED.value());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//        }
//        String token = authHeader.replace("Bearer ", "");
//        String email = jwtUtil.extractUsername(token);
//        User user = userService.findByEmail(email);
//
//        if (user == null) {
//            response.put("message", "User not found.");
//            response.put("status", HttpStatus.UNAUTHORIZED.value());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//        }
//
//        int userId = user.getId();
//        userService.updateUserFields(userId, firstName, lastName, phone, dob);
//        userService.generateUsername(firstName, lastName);
//
//        //regenerate token after updating user fields
//        String newToken = jwtUtil.generateToken(user.getEmail());
//
//        response.put("New Token", newToken);
//        response.put("message", "Updated Successfully");
//        response.put("status", HttpStatus.OK.value());
//        return ResponseEntity.ok(response);
//    }

    @PutMapping("/changeEmail")
    public ResponseEntity<Map<String, Object>> changeEmail(@RequestBody ChangeEmailRequest request,
                                                           @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token); // Extract email from JWT
        User user = userService.findByEmail(email);

        if (user ==null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String newEmail = request.getNewEmail();

        if (newEmail == null || newEmail.isEmpty() || newEmail.equals(email)) {
            response.put("message", "Invalid or duplicate email provided.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        String verificationCode = VerificationUtil.generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setEmailVerified(false);
        user.setEmail(newEmail);

        userService.save(user);

        String fName= user.getFirstName();
        String subject = "Change Email Request";
        try {
            emailService.sendVerificationEmail(newEmail,fName,subject, verificationCode);
        } catch (Exception e) {
            response.put("message", "Failed to send verification email.");
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.put("message", "Verification email sent to new email address.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verifyChangedEmail")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String verificationCode) {
        Map<String, Object> response = new HashMap<>();
        User user = userService.findByVerificationCode(verificationCode);

        if (user == null) {
            response.put("message", "Invalid verification code.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        user.setEmailVerified(true);
        user.setVerificationCode(null);
        String newToken = userService.updateUserFields(user.getId(),user.getFirstName(),user.getLastName(),user.getPhone(),user.getDob());
        response.put("New Token", newToken);
        userService.save(user);

        response.put("message", "Email verified successfully.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

//    @PutMapping("/changePassword")
//    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest request,
//                                                              @RequestHeader("Authorization") String authHeader) throws MessagingException {
//        Map<String, Object> response = new HashMap<>();
//        String code = request.getToken();
//        User user = userService.findByResetToken(code);
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            response.put("message", "Authorization header missing or invalid.");
//            response.put("status", HttpStatus.UNAUTHORIZED.value());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//        }
//
//        String token = authHeader.replace("Bearer ", "");
//        String email = jwtUtil.extractUsername(token);
//
//        System.out.println("Extracted email: " + email); // Debug
//        String newToken = userService.updateUserFields(user.getId(),user.getFirstName(),user.getLastName(),user.getPhone(),user.getDob());
//        response.put("New Token", newToken);
//        //userService.findByEmail(email);
//        if (user == null) {
//            response.put("message", "User not found.");
//            response.put("status", HttpStatus.UNAUTHORIZED.value());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//        }
//
//        userService.changePassword(user, request.getOldPassword(), request.getNewPassword());
//
//        response.put("message", "Password changed successfully.");
//        String subject = "Password Changed Successfully";
//        String body = "Your password has been changed successfully on " + user.getUpdatedAt() + ".";
//        emailService.passwordChangedEmail(user.getEmail(), user.getFirstName(), subject, body);
//
//        return ResponseEntity.ok(response);
//    }
@PutMapping("/changePassword")
public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest request,
                                                          @RequestHeader("Authorization") String authHeader) throws MessagingException {
    Map<String, Object> response = new HashMap<>();
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        response.put("message", "Authorization header missing or invalid.");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    String token = authHeader.replace("Bearer ", "");
    String email = jwtUtil.extractUsername(token);

    System.out.println("Extracted email: " + email); // Debug

    User user = userService.findByEmail(email);
    if (user == null) {
        response.put("message", "User not found.");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    userService.changePassword(user, request.getOldPassword(), request.getNewPassword());

    // Regenerate token after password change
    String newToken = jwtUtil.generateToken(user.getEmail());

    response.put("message", "Password changed successfully.");
    String subject = "Password Changed Successfully";
    String body = "Your password has been changed successfully on " + user.getUpdatedAt() + ".";
    emailService.passwordChangedEmail(user.getEmail(), user.getFirstName(), subject, body);

    response.put("New Token", newToken);
    return ResponseEntity.ok(response);
}

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<Map<String, Object>> deleteAccount(@RequestBody DeleteAccountRequest request,
                                                             @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);
        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        userService.deleteAccount(user, request.getPassword());
        response.put("message", "Account deleted successfully.");
        return ResponseEntity.ok(response);
    }
}
