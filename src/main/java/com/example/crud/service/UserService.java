package com.example.crud.service;

import com.example.crud.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(int Id);
    User save(User theUser);
    void deleteById(int Id);
    User findByEmail(String email);
    String generateUsername(String firstName, String Lastname);
    User findByVerificationCode(String code);
    User findByResetToken(String resetToken);
    String updateUserFields(int userId,String firstName,String lastName,String phone, Date dob);
    void deleteAccount(User user);
     boolean changePassword(User user, String oldPassword, String newPassword);
    public boolean isPasswordValid(User user, String password);
    public void saveDeletionToken(User user, String deletionToken);
    public boolean isDeletionTokenValid(User user, String deletionToken);
}
