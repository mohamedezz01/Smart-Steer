package com.example.crud.controllers;

import com.example.crud.entity.Authority;
import com.example.crud.dto.ResetPasswordRequest;
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

import java.util.*;

@RestController
@RequestMapping("/GP")
public class UserRestController {

    private UserService userService;
    private AuthorityService authorityService;
    private EmailService emailService;
    private VerificationUtil verficationUtil;
    private JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;


    public UserRestController(UserService theUserService, AuthorityService authorityService, EmailService emailService, PasswordEncoder passwordEncoder,JwtUtil jwtUtil) {
        this.userService = theUserService;
        this.authorityService = authorityService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil=jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody User user) throws MessagingException {

        Map<String, Object> response = new HashMap<>();

        if (userService.findByEmail(user.getEmail()) != null) {
            response.put("message", "Email already exists");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$";
        if (!user.getPassword().matches(passwordPattern)) {
            response.put("message", "Password must be at least 10 characters long and include at least one uppercase letter, one number, and one special character.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }


        String username = userService.generateUsername(user.getFirstName(), user.getLastName());
        user.setUsername(username);

        String verificationCode = VerificationUtil.generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setEmailVerified(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);

        String subject = "Email Verification";
        String body = "Please verify your email using this code: " + verificationCode;
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), subject, body);

        response.put("message", "Sign-up successful! Please verify your email.");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/verifyEmail")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String code) {
        Map<String, Object> response = new HashMap<>();

        User user = userService.findByVerificationCode(code);
        if (user == null) {
            response.put("message", "Invalid verification code.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);

        Authority auth = new Authority();
        auth.setUserId(user.getUsername());
        auth.setAuthority("ROLE_USER");
        authorityService.save(auth);

        String token = jwtUtil.generateToken(user.getUsername());

        response.put("message", "Email verified successfully!");
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

     //Login endpoint
     @PostMapping("/login")
     public ResponseEntity<Map<String, Object>> loginUser(@RequestBody User loginRequest) {
         Map<String, Object> response = new HashMap<>();

         User user = userService.findByEmail(loginRequest.getEmail());

         if (user == null) {
             response.put("message", "Email not found.");
             response.put("status", HttpStatus.UNAUTHORIZED.value());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
         }

         if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
             response.put("message", "Invalid password.");
             response.put("status", HttpStatus.UNAUTHORIZED.value());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
         }

         String token = jwtUtil.generateToken(user.getEmail());

         response.put("message", "Login successful.");
         response.put("token", token);

         return ResponseEntity.ok(response);
     }

    @PostMapping("/forgot_password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody User request) throws MessagingException {
        Map<String, Object> response = new HashMap<>();

        User user = userService.findByEmail(request.getEmail());
        if (user == null) {
            response.put("message", "Email not found.");
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Random random = new Random();
        int resetToken = 1000 + random.nextInt(9000); //number between 1000 and 9999
        user.setResetToken(String.valueOf(resetToken));
        user.setTokenExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); //valid for 15 minute
        userService.save(user);

        String subject = "Password Reset";
        String body = "Use this code to confirm your email: " + resetToken;
        emailService.passwordForgottenEmail(user.getEmail(), user.getFirstName(), subject, body);

        response.put("message", "email confirmation code sent to your email.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm_reset_code")
    public ResponseEntity<Map<String, Object>> confirmResetToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String code = request.get("token");

        User user = userService.findByResetToken(code);
        if (user == null || user.getResetToken() == null || user.getTokenExpiration().before(new Date())) {
            response.put("message", "Invalid or expired code.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        response.put("message", "Code is valid.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset_password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        String code = request.getToken();
        User user = userService.findByResetToken(code);

        if (user == null || user.getResetToken() == null || user.getTokenExpiration().before(new Date())) {
            response.put("message", "Invalid or expired code.");
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setTokenExpiration(null);
        user.setUpdatedAt(new Date());
        userService.save(user);
        //send confirmation email
        String subject = "Password Changed Successfully";
        String body = "Your password has been changed successfully on " + user.getUpdatedAt() + ".";
        emailService.passwordChangedEmail(user.getEmail(), user.getFirstName(), subject, body);

        response.put("message", "Password reset successful.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }



    ////////////////////////////////////////////////////////////////////////////////////////
    //get all users
    @GetMapping("/users")
    public List<User>findAll(){
        return userService.findAll();
    }

    //get one user
    @GetMapping("/users/{userId}")
    public User getUser(@PathVariable int userId){
        User theUser=userService.findById(userId);

        if(theUser==null){
            throw new RuntimeException("id not found "+theUser);
        }
        return theUser;
    }

    //delete user
    @DeleteMapping("/users/{Id}")
    public String delUser(@PathVariable int Id){

        User tempUser =userService.findById(Id);
        if(tempUser==null){
            throw new RuntimeException("user not found");
        }
        userService.deleteById(Id);
        return "deleted user with id of "+Id;
    }
}
