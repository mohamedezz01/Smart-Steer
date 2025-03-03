package com.example.crud.service;

import com.example.crud.entity.User;

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
    void deleteAccount(User user, String password);
    void changePassword(User user, String oldPassword, String newPassword);

}
