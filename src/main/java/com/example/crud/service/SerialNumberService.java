package com.example.crud.service;

import com.example.crud.entity.SerialNumber;
import com.example.crud.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

public interface SerialNumberService {
    ResponseEntity<Map<String, Object>> assignSerialNumber(String serialNumber, User user)throws MessagingException;
    Optional<SerialNumber> findBySerialNumber(String serialNumber);
}