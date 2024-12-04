package com.example.crud.service;

import com.example.crud.dao.UserRepository;
import com.example.crud.entity.User;
import com.example.crud.util.VerificationUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{


    private UserRepository userRepository;
    private VerificationUtil verfificationUtil;
    @Autowired
    private EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserServiceImpl(UserRepository theUserRepository , PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = theUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void signUpUser(User user) throws MessagingException {

        String verificationCode = VerificationUtil.generateVerificationCode();
        user.setVerificationCode(verificationCode);

//        String encryptedPassword = passwordEncoder.encode(user.getPassword());
//        user.setPassword(encryptedPassword);

        userRepository.save(user);

    }


    public User save(User user) {
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(int Id) {
        Optional<User> Result = userRepository.findById(Id);

        User theUser=null;

        if (Result.isPresent()){
            theUser=Result.get();
        }
        else {
            throw new RuntimeException("didn't found the user id");
        }
        return theUser;
    }


    public User findByVerificationCode(String verificationCode) {
        return userRepository.findByVerificationCode(verificationCode);
    }

    @Override
    public User findByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken);
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
}
