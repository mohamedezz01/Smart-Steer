package com.example.crud.service;

import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public interface EmailServ {

     void sendVerificationEmail(String to,String firstName, String subject, String body)throws MessagingException;
     void passwordChangedEmail(String to,String firstName, String subject, String body)throws MessagingException;
     void accountDeletedEmail(String to, String firstName, Locale locale, Date updatedAt)throws MessagingException;
     void passwordForgottenEmail(String to,String firstName, String subject, String resetToken)throws MessagingException;
}