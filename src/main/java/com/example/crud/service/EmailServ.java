package com.example.crud.service;

import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface EmailServ {

     @Async
     void sendVerificationEmail(String to,String firstName, String subject, String body)throws MessagingException;
     @Async
     void passwordChangedEmail(String to,String firstName, String subject, String body)throws MessagingException;
     @Async
     void accountDeletedEmail(String to,String firstName, String subject, String body)throws MessagingException;
     @Async
     void passwordForgottenEmail(String to,String firstName, String subject, String resetToken)throws MessagingException;
     @Async
     void ownerEmail(String to,String firstName,String lastName, String subject)throws MessagingException;
     @Async
    void Sendnotify(String to, String firstName, String addedByName, String addedByPhone, String phone) throws MessagingException;
     @Async
     public void sendEmergencyEmails(User user, List<EmergencyContact> contacts, String location);
}