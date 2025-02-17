package com.example.crud.controllers;

import com.example.crud.dto.ChangeEmailRequest;
import com.example.crud.dto.ChangePasswordRequest;
import com.example.crud.dto.DeleteAccountRequest;
import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import com.example.crud.service.AuthorityService;
import com.example.crud.service.EmailService;
import com.example.crud.service.TokenBlacklistService;
import com.example.crud.service.UserService;
import com.example.crud.util.JwtUtil;
import com.example.crud.util.VerificationUtil;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/GP/settings")
public class SettingsRestController {

    private UserService userService;
    private AuthorityService authorityService;
    private EmailService emailService;
    private VerificationUtil verficationUtil;
    private JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private TokenBlacklistService tokenBlacklistService;

    public SettingsRestController(UserService theUserService, AuthorityService authorityService, EmailService emailService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.userService = theUserService;
        this.authorityService = authorityService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil=jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }


    @PostMapping("/verifyCurrentEmail")
    public ResponseEntity<Map<String, Object>> verifyCurrentEmail(@RequestHeader("Authorization") String authHeader) throws MessagingException {
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
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String verificationCode = VerificationUtil.generateVerificationCode();
        user.setVerificationCode(verificationCode);
        userService.save(user);

        String subject = "Confirm Your Current Email";
        String body = "Your verification code: " + verificationCode;
        emailService.sendVerificationEmail(email, user.getFirstName(), subject, body);

        response.put("message", "Verification code sent to current email.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirmCurrentEmail")
    public ResponseEntity<Map<String, Object>> confirmCurrentEmail(@RequestBody Map<String, String> requestBody,
                                                                   @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        String verificationCode = requestBody.get("verificationCode");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        if (verificationCode == null || verificationCode.isEmpty()) {
            response.put("message", "Verification code is required.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null || !verificationCode.equals(user.getVerificationCode())) {
            response.put("message", "Invalid verification code.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        user.setVerificationCode(null);
        userService.save(user);

        response.put("message", "Current email verified successfully");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sendNewEmailVerification")
    public ResponseEntity<Map<String, Object>> sendNewEmailVerification(@RequestBody Map<String, String> requestBody,
                                                                        @RequestHeader("Authorization") String authHeader) throws MessagingException {
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
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        String newEmail = requestBody.get("newEmail");
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

        String newToken = jwtUtil.generateToken(user.getUsername(), user.getEmail());
        response.put("New Token", newToken);

        String subject = "Confirm Your New Email";
        String body = "Your verification code: " + verificationCode;
        emailService.sendVerificationEmail(newEmail, user.getFirstName(), subject, body);

        response.put("message", "Verification code sent to new email.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirmNewEmail")
    public ResponseEntity<Map<String, Object>> confirmNewEmail(@RequestBody Map<String, String> requestBody,
                                                               @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);  //extract email from token
        User user = userService.findByEmail(email);  //find user by extracted email

        if (user == null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String verificationCode = requestBody.get("verificationCode");

        if (verificationCode == null || verificationCode.isEmpty() || !verificationCode.equals(user.getVerificationCode())) {
            response.put("message", "Invalid verification code.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userService.save(user);

        String newToken = jwtUtil.generateToken(user.getUsername(), user.getEmail());

        response.put("New Token", newToken);
        response.put("message", "New email verified and updated successfully.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> email(@RequestHeader("Authorization") String authHeader) {
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

        response.put("message", email);
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest request,
                                                              @RequestHeader("Authorization") String authHeader) throws MessagingException {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            response.put("message", "Old password can't be the same as the new password.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$";
        if (!request.getNewPassword().matches(passwordPattern)) {
            response.put("message", "Password must be at least 10 characters long and include at least one uppercase letter, one number, and one special character.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", "User not found.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (!userService.isPasswordValid(user, request.getOldPassword())) {
            response.put("message", "Old password is incorrect.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        userService.changePassword(user, request.getOldPassword(), request.getNewPassword());

        String newToken = jwtUtil.generateToken(user.getUsername(), user.getEmail());

        response.put("message", "Password changed successfully.");
        response.put("New Token", newToken);

        String subject = "Password Changed Successfully";
        String body = "Your password has been changed successfully on " + user.getUpdatedAt() + ".";
        emailService.passwordChangedEmail(user.getEmail(), user.getFirstName(), subject, body);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify_delAcc")
    public ResponseEntity<Map<String, Object>> verifyDeleteAccount(@RequestBody DeleteAccountRequest request) {
        Map<String, Object> response = new HashMap<>();
        User user = userService.findByEmail(request.getEmail());

        if (user == null) {
            response.put("message", "Invalid email.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            response.put("message", "Invalid password.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String deletionToken = jwtUtil.generateDeletionToken(user.getEmail());
        userService.saveDeletionToken(user, deletionToken);

        response.put("message", "Are you sure you want to delete your account?");
        response.put("deletionToken", deletionToken);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/confirm_delAcc")
    public ResponseEntity<Map<String, Object>> confirmDeleteAccount(@RequestHeader("Deletion-Token") String deletionToken) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        String delToken = jwtUtil.validateDeletionToken(deletionToken);

        User user = userService.findByEmail(delToken);
        if (user == null || !userService.isDeletionTokenValid(user, deletionToken)) {
            response.put("message", "Invalid or expired deletion token");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        userService.deleteAccount(user);
        emailService.accountDeletedEmail(user.getEmail(), user.getFirstName(), "Account deleted successfully", "Your account was deleted on " + user.getUpdatedAt() + ".");

        response.put("message", "Account deleted successfully");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        //validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Authorization header missing or invalid.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        //extract the token
        String token = authHeader.replace("Bearer ", "");

        //blacklist the token
        tokenBlacklistService.blacklistToken(token);

        response.put("message", "Logged out successfully.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

}
