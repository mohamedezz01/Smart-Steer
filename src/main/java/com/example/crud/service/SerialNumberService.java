package com.example.crud.service;

import com.example.crud.entity.SerialNumber;
import com.example.crud.entity.User;
import jakarta.mail.MessagingException;

import java.util.Optional;

public interface SerialNumberService {
    String assignSerialNumber(String serialNumber, User user) throws MessagingException;
    Optional<SerialNumber> findBySerialNumber(String serialNumber);
}