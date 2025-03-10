package com.example.crud.service;

import com.example.crud.entity.SerialNumber;
import com.example.crud.entity.User;
import com.example.crud.dao.SerialNumberRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SerialNumberServiceImpl implements SerialNumberService {

    private final SerialNumberRepository serialNumberRepository;
    private final EmailService emailService;

    @Autowired
    public SerialNumberServiceImpl(SerialNumberRepository serialNumberRepository,
                                   EmailService emailService) {
        this.serialNumberRepository = serialNumberRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public String assignSerialNumber(String serialNumber, User user) throws MessagingException {
        Optional<SerialNumber> serialNumberOpt = serialNumberRepository.findBySerialNumber(serialNumber);

        if (serialNumberOpt.isEmpty() || serialNumberOpt.get().isAssigned()) {
            return "Invalid or already assigned serial number.";
        }

        SerialNumber serial = serialNumberOpt.get();
        serial.setAssigned(true);
        serial.setUser(user);
        serialNumberRepository.save(serial);

        user.setRoles("ROLE_OWNER");

        String subject = "Thank you for choosing Smart Steer";
        emailService.ownerEmail(user.getEmail(), user.getFirstName(), user.getLastName(), subject);

        return "Serial number assigned successfully.";
    }

    @Override
    public Optional<SerialNumber> findBySerialNumber(String serialNumber) {
        return serialNumberRepository.findBySerialNumber(serialNumber);
    }
}