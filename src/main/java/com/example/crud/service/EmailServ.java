package com.example.crud.service;

import jakarta.mail.MessagingException;

public interface EmailServ {

     void sendVerificationEmail(String to,String firstName, String subject, String body)throws MessagingException;
     void passwordChangedEmail(String to,String firstName, String subject, String body)throws MessagingException;
     void accountDeletedEmail(String to,String firstName, String subject, String body)throws MessagingException;
     void passwordForgottenEmail(String to,String firstName, String subject, String resetToken)throws MessagingException;
     void ownerEmail(String to,String firstName,String lastName, String subject)throws MessagingException;

}