package com.example.crud.rest;

import com.example.crud.entity.Authority;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/GP")
public class UserRestController {

    private UserService userService;
    private AuthorityService authorityService;
    private EmailService emailService;
    private VerificationUtil verficationUtil;
    private JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;


    public UserRestController(UserService theUserService, AuthorityService authorityService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userService = theUserService;
        this.authorityService = authorityService;
        this.emailService=emailService;
        this.passwordEncoder = passwordEncoder;
    }
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody User user) throws MessagingException {

        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$";

        if (!user.getPassword().matches(passwordPattern)) {
            return ResponseEntity.badRequest().body("Password must be at least 10 characters long and include at least one uppercase letter, one number, and one special character.");
        }

        if (!user.getPassword().equals(user.getConfirmPass())) {
            return ResponseEntity.badRequest().body("Passwords do not match.");
        }
        String username = userService.generateUsername(user.getFirstName(),user.getLastName());
        user.setUsername(username);

        String verificationCode = VerificationUtil.generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setEmailVerified(false);
        userService.save(user);

        String subject = "Email Verification";
        String body = "Please verify your email using this code: " + verificationCode;

        emailService.sendVerificationEmail(user.getEmail(),user.getFirstName(), subject, body);


        return ResponseEntity.ok("Sign-up successful! Please verify your email.");

    }


    @PostMapping("/verifyEmail")
    public ResponseEntity<String> verifyEmail(@RequestParam String code) {

        User user = userService.findByVerificationCode(code);
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid verification code.");
        }
        user.setEmailVerified(true);
        user.setVerificationCode(null);
        // userService.save(user);
        User savedUser = userService.save(user);


        Authority auth = new Authority();
        auth.setUserId(user.getUsername());
        auth.setAuthority("ROLE_USER");
        authorityService.save(auth);
        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok("Email verified successfully!"+token);
    }


    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User loginRequest) {
        User user = userService.findByEmail(loginRequest.getEmail());

        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {

            String token = jwtUtil.generateToken(user.getUsername());

            return ResponseEntity.ok("Login successful for user: " + user.getUsername() + ", Token: " + token);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login credentials.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        //token

        return ResponseEntity.ok("User logged out successfully");
    }
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


    @PutMapping("/users")
    public User updateUser(@PathVariable int userId, @RequestBody Map<String, Object> updates) {

        // Fetch the existing user by ID
        User existingUser = userService.findById(userId);
        if (existingUser == null) {
            throw new RuntimeException("User ID not found - " + userId);
        }

        updates.forEach((key, value) -> {
            switch (key) {
                case "firstName":
                    existingUser.setFirstName((String) value);
                    break;
                case "lastName":
                    existingUser.setLastName((String) value);
                    break;
                case "email":
                    existingUser.setEmail((String) value);
                    break;
                case "password":
                    existingUser.setPassword((String) value);
                    break;

                case "phone":
                    existingUser.setPhone((String) value);
                    break;

                case "dob":
                    existingUser.setDob((Date) value);
            }
        });

        return userService.save(existingUser);
    }

    //update user
//    @PutMapping("/users")
//    public User updUser(@RequestBody User theUser){
//
//        User updUser=userService.save(theUser);
//        return updUser;
//    }

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

    //add new user (SIGN UP)


}
