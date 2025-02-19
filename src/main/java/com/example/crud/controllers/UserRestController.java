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
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody User user,
                                                      @RequestHeader(value = "Accept-Language", required = false) String lang) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        User existingUser = userService.findByEmail(user.getEmail());

        //if email exists but not verified, resend verification code
        if (existingUser != null) {
            if (!existingUser.isEmailVerified()) {
                String newVerificationCode = VerificationUtil.generateVerificationCode();
                existingUser.setVerificationCode(newVerificationCode);
                userService.save(existingUser);

                String subject = messageSource.getMessage("email.resend.verification.subject", null, locale);
                String body = messageSource.getMessage("verification.code.sent", new Object[]{newVerificationCode}, locale);
                emailService.sendVerificationEmail(existingUser.getEmail(), existingUser.getFirstName(), subject, body);

                response.put("message", messageSource.getMessage("verification.code.sent", null, locale));
                return ResponseEntity.ok(response);
            }

            //email already exists and verified
            response.put("message",messageSource.getMessage("email.already.exists", null, locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        //new user signup Process
        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$";
        if (!user.getPassword().matches(passwordPattern)) {
            response.put("message", messageSource.getMessage("password.invalid.format", null, locale));
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

        String subject =  messageSource.getMessage("email.verification.subject", null, locale);
        String body = messageSource.getMessage("email.verification.body", new Object[]{verificationCode}, locale);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), subject, body);

        response.put("message", messageSource.getMessage("signup.success",null,locale));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verifyEmail")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String code,@RequestHeader(value = "Accept-Language", required = false) String lang) {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        User user = userService.findByVerificationCode(code);
        if (user == null) {
            response.put("message",messageSource.getMessage("invalid.verification.code", null, locale));
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

        response.put("message", messageSource.getMessage("email.verified.success", null, locale));
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
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody User request,
                                                              @RequestHeader(value = "Accept-Language", required = false) String lang) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        User user = userService.findByEmail(request.getEmail());
        if (user == null) {
            response.put("message", messageSource.getMessage("email.not.found", null,locale));
            response.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Random random = new Random();
        int resetToken = 1000 + random.nextInt(9000); //number between 1000 and 9999
        user.setResetToken(String.valueOf(resetToken));
        user.setTokenExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); //valid for 15 minute
        userService.save(user);

        String subject =messageSource.getMessage("Password.Reset", null, locale);
        String body = messageSource.getMessage("Password.Reset.Body", new Object[]{resetToken}, locale);
        emailService.passwordForgottenEmail(user.getEmail(), user.getFirstName(), subject, body);

        response.put("message", messageSource.getMessage("email.confirm", null, locale));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm_reset_code")
    public ResponseEntity<Map<String, Object>> confirmResetToken(@RequestBody Map<String, String> request,
                                                                 @RequestHeader(value = "Accept-Language", required = false) String lang) {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        String code = request.get("token");

        User user = userService.findByResetToken(code);
        if (user == null || user.getResetToken() == null || user.getTokenExpiration().before(new Date())) {
            response.put("message", messageSource.getMessage("invalid.verification.code", null, locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("message", messageSource.getMessage("valid", null, locale));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset_password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request,
                                                             @RequestHeader(value = "Accept-Language", required = false) String lang) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        String code = request.getToken();
        User user = userService.findByResetToken(code);

        if (user == null || user.getResetToken() == null || user.getTokenExpiration().before(new Date())) {
            response.put("message", messageSource.getMessage("invalid.verification.code", null, locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$";

        if (!request.getNewPassword().matches(passwordPattern)) {
            response.put("message", messageSource.getMessage("password.invalid.format", null, locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setTokenExpiration(null);
        user.setUpdatedAt(new Date());
        userService.save(user);
        //send confirmation email
        String subject = messageSource.getMessage("pass.success.subject", null, locale);
        String body = messageSource.getMessage("pass.success.body", null, locale);
        emailService.passwordChangedEmail(user.getEmail(), user.getFirstName(), subject, body);

        response.put("message", messageSource.getMessage("pass.success", null, locale));
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resendVerification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, String> requestBody,
                                                                  @RequestHeader(value = "Accept-Language", required = false) String lang) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        String email = requestBody.get("email");

        if (email == null || email.isEmpty()) {
            response.put("message", messageSource.getMessage("email.not.found", null, locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        User user;
            user = userService.findByEmail(email);

            if (!user.getEmail().equals(email)) {
                    response.put("message", messageSource.getMessage("resend", null, locale));
                response.put("status", HttpStatus.FORBIDDEN.value());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        if (user.isEmailVerified()) {
            response.put("message", messageSource.getMessage("already.verified", null, locale));
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }

        String newVerificationCode = VerificationUtil.generateVerificationCode();
        user.setVerificationCode(newVerificationCode);
        userService.save(user);

        String subject = messageSource.getMessage("email.resend.verification.subject", null, locale);
        String body = messageSource.getMessage("resend.body", new Object[]{newVerificationCode}, locale);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), subject, body);

        response.put("message",messageSource.getMessage("verification.code.sent", null, locale));
        return ResponseEntity.ok(response);
}
    @PostMapping("/resendForgot")
    public ResponseEntity<Map<String, Object>> resendForgot(@RequestBody Map<String, String> requestBody,
                                                            @RequestHeader(value = "Accept-Language", required = false) String lang) throws MessagingException {
        Map<String, Object> response = new HashMap<>();
        Locale locale = (lang != null && lang.equalsIgnoreCase("ar")) ? new Locale("ar") : new Locale("en");
        String email = requestBody.get("email");

        if (email == null || email.isEmpty()) {
            response.put("message", messageSource.getMessage("email.not.found", null, locale));
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


        String subject = messageSource.getMessage("email.resend.verification.subject", null, locale);
        String body = messageSource.getMessage("resend.body", new Object[]{resetToken}, locale);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), subject, body);
        response.put("message",messageSource.getMessage("verification.code.sent", null, locale));
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
