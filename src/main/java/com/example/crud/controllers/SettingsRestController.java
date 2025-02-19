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
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private MessageSource messageSource;

    public SettingsRestController(UserService theUserService, AuthorityService authorityService, EmailService emailService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService,MessageSource messageSource) {
        this.userService = theUserService;
        this.authorityService = authorityService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil=jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.messageSource=messageSource;
    }


    @PostMapping("/verifyCurrentEmail")
    public ResponseEntity<Map<String, Object>> verifyCurrentEmail(@RequestHeader("Authorization") String authHeader,
                                                                  @RequestHeader(value = "Accept-Language", required = false) String lang) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", "email.not.found");
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String verificationCode = VerificationUtil.generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setEmailVerified(false);
        userService.save(user);

        String subject =messageSource.getMessage("c.c.e",null,locale);
        String body = messageSource.getMessage("email.verification.body", new Object[]{verificationCode}, locale);
        emailService.sendVerificationEmail(email, user.getFirstName(), subject, body);

        response.put("message", messageSource.getMessage("verification.code.sent", null, locale));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirmCurrentEmail")
    public ResponseEntity<Map<String, Object>> confirmCurrentEmail(@RequestBody Map<String, String> requestBody,
                                                                   @RequestHeader("Authorization") String authHeader,
                                                                   @RequestHeader(value = "Accept-Language", required = false) String lang)  {
        Map<String, Object> response = new HashMap<>();
        String verificationCode = requestBody.get("verificationCode");
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (verificationCode == null || verificationCode.isEmpty()) {
            response.put("message", messageSource.getMessage("invalid.verification.code",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null || !verificationCode.equals(user.getVerificationCode())) {
            response.put("message", messageSource.getMessage("invalid.verification.code",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userService.save(user);

        response.put("message",messageSource.getMessage("email.verified.success",null,locale));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sendNewEmailVerification")
    public ResponseEntity<Map<String, Object>> sendNewEmailVerification(
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "Accept-Language", required = false) String lang)  throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        // Validate Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found",null,locale));
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String newEmail = requestBody.get("newEmail");
        if (newEmail == null || newEmail.isEmpty() || newEmail.equals(email)) {
            response.put("message", messageSource.getMessage("invalid.or.duplicate.email",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            String verificationCode = VerificationUtil.generateVerificationCode();
            user.setVerificationCode(verificationCode);
            user.setEmailVerified(false);
            user.setEmail(newEmail);
            userService.save(user);

            String newToken = jwtUtil.generateToken(user.getUsername(), user.getEmail());
            response.put("New Token", newToken);

            String subject = messageSource.getMessage("new.email.subject",null,locale);
            String body = messageSource.getMessage("email.verification.body", new Object[]{verificationCode}, locale);
            emailService.sendVerificationEmail(newEmail, user.getFirstName(), subject, body);


            response.put("message", messageSource.getMessage("verification.code.sent", null, locale));
            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException ex) {
            // Handle duplicate email error
            response.put("message", "email.already.exists");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception ex) {
            // Handle other unexpected errors
            response.put("message", messageSource.getMessage("unexpected.error", null, locale));
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/confirmNewEmail")
    public ResponseEntity<Map<String, Object>> confirmNewEmail(@RequestBody Map<String, String> requestBody,
                                                               @RequestHeader("Authorization") String authHeader,
                                                               @RequestHeader(value = "Accept-Language", required = false) String lang)  {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);  //extract email from token
        User user = userService.findByEmail(email);  //find user by extracted email

        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found",null,locale));
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String verificationCode = requestBody.get("verificationCode");

        if (verificationCode == null || verificationCode.isEmpty() || !verificationCode.equals(user.getVerificationCode())) {
            response.put("message", messageSource.getMessage("invalid.verification.code",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userService.save(user);

        String newToken = jwtUtil.generateToken(user.getUsername(), user.getEmail());

        response.put("New Token", newToken);
        response.put("message", messageSource.getMessage("new.email.success",null,locale));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> email(@RequestHeader("Authorization") String authHeader,
                                                     @RequestHeader(value = "Accept-Language", required = false) String lang)  {
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

        response.put("message", email);
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest request,
                                                              @RequestHeader("Authorization") String authHeader,
                                                              @RequestHeader(value = "Accept-Language", required = false) String lang)  throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            response.put("message",messageSource.getMessage("pass.new",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$";
        if (!request.getNewPassword().matches(passwordPattern)) {
            response.put("message", messageSource.getMessage("password.pattern.invalid",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (!userService.isPasswordValid(user, request.getOldPassword())) {
            response.put("message",messageSource.getMessage("pass.old",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        userService.changePassword(user, request.getOldPassword(), request.getNewPassword());

        String newToken = jwtUtil.generateToken(user.getUsername(), user.getEmail());

        response.put("message", messageSource.getMessage("pass.success.subject",null,locale));
        response.put("New Token", newToken);

        String subject = messageSource.getMessage("pass.success.subject",null,locale);
        String body = messageSource.getMessage("pass.success.body", null, locale);
        emailService.passwordChangedEmail(user.getEmail(), user.getFirstName(), subject, body);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify_delAcc")
    public ResponseEntity<Map<String, Object>> verifyDeleteAccount(@RequestBody DeleteAccountRequest request,
                                                                   @RequestHeader(value = "Accept-Language", required = false) String lang)  {
        Map<String, Object> response = new HashMap<>();
        User user = userService.findByEmail(request.getEmail());
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            response.put("message", messageSource.getMessage("invalid.password",null,locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String deletionToken = jwtUtil.generateDeletionToken(user.getEmail());
        userService.saveDeletionToken(user, deletionToken);

        response.put("message", messageSource.getMessage("verify.del.acc.subject",null,locale));
        response.put("deletionToken", deletionToken);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/confirm_delAcc")
    public ResponseEntity<Map<String, Object>> confirmDeleteAccount(@RequestHeader("Deletion-Token") String deletionToken,
                                                                    @RequestHeader(value = "Accept-Language", required = false) String lang)  throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        String delToken = jwtUtil.validateDeletionToken(deletionToken);
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        User user = userService.findByEmail(delToken);
        if (user == null || !userService.isDeletionTokenValid(user, deletionToken)) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        userService.deleteAccount(user);

        emailService.accountDeletedEmail(user.getEmail(), user.getFirstName(), locale, user.getUpdatedAt());

        response.put("message", messageSource.getMessage("account.deleted.subject",null,locale));
        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String authHeader,
                                                      @RequestHeader(value = "Accept-Language", required = false) String lang)  {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        //validate the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        //extract the token
        String token = authHeader.replace("Bearer ", "");

        //blacklist the token
        tokenBlacklistService.blacklistToken(token);

        response.put("message", messageSource.getMessage("logout.success",null,locale));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    //profile picture upload
    @PostMapping("/uploadProfilePicture")
    public ResponseEntity<Map<String, Object>> uploadProfilePicture(@RequestParam("image") MultipartFile file,
                                                                    @RequestHeader("Authorization") String authHeader,
                                                                    @RequestHeader(value = "Accept-Language", required = false) String lang)  {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", messageSource.getMessage("authorization.header.invalid",null,locale));
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        //2MB max
        if (file.getSize() > 2 * 1024 * 1024) {
            response.put("message", messageSource.getMessage("file.too.large",null,locale));
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found",null,locale));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        try {
            user.setProfilePicture(file.getBytes());
            userService.save(user);
            response.put("message", messageSource.getMessage("profile.picture.uploaded",null,locale));
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("message", messageSource.getMessage("image.failed",null,locale));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/profilePicture")
    public ResponseEntity<byte[]> getProfilePicture(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null || user.getProfilePicture() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(user.getProfilePicture());
    }


}
