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
import org.springframework.context.MessageSource;
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
    private MessageSource messageSource;


    public UserRestController(UserService theUserService, AuthorityService authorityService, EmailService emailService, PasswordEncoder passwordEncoder,JwtUtil jwtUtil,MessageSource messageSource) {
        this.userService = theUserService;
        this.authorityService = authorityService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil=jwtUtil;
        this.messageSource=messageSource;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody User user) throws MessagingException {
        Map<String, Object> response = new HashMap<>();

        User existingUser = userService.findByEmail(user.getEmail());

        //if email exists but not verified, resend verification code
        if (existingUser != null) {
            if (!existingUser.isEmailVerified()) {
                String newVerificationCode = VerificationUtil.generateVerificationCode();
                existingUser.setVerificationCode(newVerificationCode);
                userService.save(existingUser);

                String subject = "Resend Email Verification";
                String body = "Please verify your email using this new code: " + newVerificationCode;
                emailService.sendVerificationEmail(existingUser.getEmail(), existingUser.getFirstName(), subject, body);

                response.put("message", "A new verification code has been sent. Please verify your email.");
                return ResponseEntity.ok(response);
            }

            //email already exists and verified
            response.put("message", "Email already exists");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        //new user signup Process
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

        String token = jwtUtil.generateToken(user.getUsername(), user.getEmail());

        response.put("message", "Email verified successfully!");
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
     //Login endpoint
     @PostMapping("/login")
     public ResponseEntity<Map<String, Object>> loginUser(@RequestBody User loginRequest,
                                                          @RequestHeader(value = "Accept-Language", required = false) String lang) throws MessagingException {
         Map<String, Object> response = new HashMap<>();
         Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
         User user = userService.findByEmail(loginRequest.getEmail());

         if (user == null) {
             response.put("message", messageSource.getMessage("email.not.found", null, locale));
             response.put("status", HttpStatus.BAD_REQUEST.value());
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
         }

         if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
             response.put("message", messageSource.getMessage("invalid.password", null, locale));
             response.put("status", HttpStatus.BAD_REQUEST.value());
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
         }

         if (!user.isEmailVerified()) {
             response.put("message", messageSource.getMessage("email.not.verified", null, locale));
             response.put("status", HttpStatus.FORBIDDEN.value());

             String verificationCode = VerificationUtil.generateVerificationCode();
             user.setVerificationCode(verificationCode);
             String subject = messageSource.getMessage("email.verification.subject", null, locale);
             String body = messageSource.getMessage("email.verification.body", new Object[]{verificationCode}, locale);
             emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), subject, body);
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
         }

         String token = jwtUtil.generateToken(user.getUsername(), user.getEmail());

         response.put("message", messageSource.getMessage("login.success", null, locale));
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
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$";
        System.out.println("New Password: " + request.getNewPassword());
        System.out.println("Pattern Matches: " + request.getNewPassword().matches(passwordPattern));
        if (!request.getNewPassword().matches(passwordPattern)) {
            response.put("message", "Password must be at least 10 characters long and include at least one lowercase letter, one uppercase letter, one number, and one special character.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
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

    @PostMapping("/resendVerification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, String> requestBody) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        String email = requestBody.get("email");

        if (email == null || email.isEmpty()) {
            response.put("message", "Email is required.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        User user;
            user = userService.findByEmail(email);

            if (!user.getEmail().equals(email)) {
                response.put("message", "You can only request verification for your pending email.");
                response.put("status", HttpStatus.FORBIDDEN.value());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        if (user.isEmailVerified()) {
            response.put("message", "Email is already verified.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        String newVerificationCode = VerificationUtil.generateVerificationCode();
        user.setVerificationCode(newVerificationCode);
        userService.save(user);

        String subject = "Resend Email Verification";
        String body = "Please verify your email using this new code: " + newVerificationCode;
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), subject, body);

        response.put("message", "A new verification code has been sent to your email");
        return ResponseEntity.ok(response);
}
    @PostMapping("/resendForgot")
    public ResponseEntity<Map<String, Object>> resendForgot(@RequestBody Map<String, String> requestBody) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        String email = requestBody.get("email");

        if (email == null || email.isEmpty()) {
            response.put("message", "Email is required.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        User user;
        user = userService.findByEmail(email);

        Random random = new Random();
        int resetToken = 1000 + random.nextInt(9000); //number between 1000 and 9999
        user.setResetToken(String.valueOf(resetToken));
        user.setTokenExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); //valid for 15 minute
        userService.save(user);


        String subject = "Resend Email Verification";
        String body = "Please verify your email using this new code: " + resetToken;
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), subject, body);

        response.put("message", "A new verification code has been sent to your email");
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
