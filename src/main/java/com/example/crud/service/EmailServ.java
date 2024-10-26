package com.example.crud.service;

import jakarta.mail.MessagingException;

public interface EmailServ {

     void sendVerificationEmail(String to,String firstName, String subject, String body)throws MessagingException;

}
