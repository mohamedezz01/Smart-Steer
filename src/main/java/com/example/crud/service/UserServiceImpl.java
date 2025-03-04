package com.example.crud.service;

import com.example.crud.dao.UserRepository;
import com.example.crud.entity.User;
import com.example.crud.util.JwtUtil;
import com.example.crud.util.VerificationUtil;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private VerificationUtil verfificationUtil;
    @Autowired
    private EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Autowired
    public UserServiceImpl(UserRepository theUserRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, EmailService emailService) { // Updated constructor name
        this.userRepository = theUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;

    }

    public void signUpUser(User user) throws MessagingException {
        String verificationCode = VerificationUtil.generateVerificationCode();
        user.setVerificationCode(verificationCode);
        if (!user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
    }

    public User save(User user) {
        if (!user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public boolean isPasswordValid(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword()); // ✅ This is correct
    }

    @Override
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        // No need to revalidate the old password (already checked in the controller)
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }



    @Override
    public void deleteById(int Id) {
        userRepository.deleteById(Id);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName.toLowerCase() + "_" + lastName.toLowerCase();
        String username = baseUsername;
        int count = 1;

        while (userRepository.findByUsername(username) != null) {
            username = baseUsername + count;
            count++;
        }
        return username;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(int Id) {
        Optional<User> Result = userRepository.findById(Id);

        User user = null;

        if (Result.isPresent()) {
            user = Result.get();
        } else {
            throw new RuntimeException("didn't found the user id");
        }
        return user;
    }

    public User findByVerificationCode(String verificationCode) {
        return userRepository.findByVerificationCode(verificationCode);
    }

    @Override
    public User findByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken);
    }

    @Override
    public String updateUserFields(int userId, String firstName, String lastName, String phone, Date dob) {
        User user = findById(userId);
        List<String> roles = Arrays.asList(user.getRoles().split(","));
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (phone != null) user.setPhone(phone);
        if (dob != null) user.setDob(dob);
        save(user);

        return jwtUtil.generateToken(user.getEmail(),user.getUsername(),roles);
    }



    @Override
    public void deleteAccount(User user) {
        userRepository.delete(user);
    }

    @Override
    public void saveDeletionToken(User user, String deletionToken) {
        user.setDeletionToken(deletionToken);
        user.setDeletionTokenExpiry(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10))); // 10 min expiry
        userRepository.save(user);
    }
    @Override
    public boolean isDeletionTokenValid(User user, String deletionToken) {
        return user.getDeletionToken() != null &&
                user.getDeletionToken().equals(deletionToken) &&
                user.getDeletionTokenExpiry() != null &&
                user.getDeletionTokenExpiry().after(new Date()); // Check expiry
    }
}