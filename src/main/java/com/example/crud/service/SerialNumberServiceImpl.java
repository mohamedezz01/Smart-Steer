package com.example.crud.service;

import com.example.crud.entity.SerialNumber;
import com.example.crud.entity.User;
import com.example.crud.dao.SerialNumberRepository;
import com.example.crud.util.JwtUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SerialNumberServiceImpl implements SerialNumberService {

    private final SerialNumberRepository serialNumberRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    @Autowired
    public SerialNumberServiceImpl(SerialNumberRepository serialNumberRepository, EmailService emailService,JwtUtil jwtUtil) {
        this.serialNumberRepository = serialNumberRepository;
        this.emailService = emailService;
        this.jwtUtil=jwtUtil;
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> assignSerialNumber(String serialNumber, User user) throws MessagingException {
        //hash the incoming serial number
        String hashedSerialNumber = hashSerialNumber(serialNumber);
        Map<String, Object> response = new HashMap<>();

        //find the serial number by its hashed value
        Optional<SerialNumber> serialNumberOpt = serialNumberRepository.findBySerialNumber(hashedSerialNumber);

        if (serialNumberOpt.isEmpty() || serialNumberOpt.get().isAssigned()){
            response.put("message", "Invalid serial number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        SerialNumber serial = serialNumberOpt.get();
        serial.setAssigned(true);
        serial.setUser(user);
        serialNumberRepository.save(serial);

        user.setRoles("ROLE_OWNER");

        String subject = "Thank you for choosing Smart Steer";
        emailService.ownerEmail(user.getEmail(), user.getFirstName(), user.getLastName(), subject);

        List<String> roles = Arrays.asList(user.getRoles().split(","));
        String newToken = jwtUtil.generateToken(user.getUsername(), user.getEmail(),roles);

        response.put("New Token", newToken);
        response.put("message", "Serial number linked to your account");
        return ResponseEntity.ok(response);
    }

    //hash the serial number
    private String hashSerialNumber(String serialNumber) {
        //using SHA-256
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(serialNumber);
    }
    @Override
    public Optional<SerialNumber> findBySerialNumber(String serialNumber) {
        return serialNumberRepository.findBySerialNumber(serialNumber);
    }
}