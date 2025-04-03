package com.example.crud.service;

import com.example.crud.dao.EmergencyContactRepository;
import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EmergencyContactServiceImpl implements EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final UserService userService;
    @Autowired
    private EmailServ emailService;

    public EmergencyContactServiceImpl(EmergencyContactRepository emergencyContactRepository,
                                       UserService userService,
                                       EmailServ emailService) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Cacheable(value = "userContacts", key = "#userId")
    @Override
    public List<EmergencyContact> getContactsByUserId(int userId) {
        return emergencyContactRepository.findAllByUserId(userId);
    }

    @CacheEvict(value = "userContacts", key = "#contact.user.id")
    @Override
    public EmergencyContact addContact(EmergencyContact contact) {
        return emergencyContactRepository.save(contact);
    }

    @Override
    public boolean existsByPhoneAndUser(String phone, User user) {
        return emergencyContactRepository.existsByPhoneAndUser(phone, user);
    }

    @CacheEvict(value = {"userContacts", "contact"}, allEntries = true)
    @Override
    public void deleteContact(int contactId) {
        emergencyContactRepository.deleteById(contactId);
    }

    @Override
    public void notifyUserIfPhoneExists(String phone, String addedByName, String addedByPhone) {
        User existingUser = userService.findByPhone(phone);
        if (existingUser != null && existingUser.getEmail() != null) {
            try {
                emailService.Sendnotify(
                        existingUser.getEmail(),
                        existingUser.getFirstName(),
                        addedByName,
                        addedByPhone,
                        phone
                );
            } catch (MessagingException e) {
                log.error("Failed to send email to: {}", existingUser.getEmail(), e);
            }
        } else {
            log.warn("User found but email is missing: {}", phone);
        }
    }


    @Cacheable(value = "contact", key = "#id")
    @Override
    public EmergencyContact findById(int id) {
        return emergencyContactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found!"));
    }
}