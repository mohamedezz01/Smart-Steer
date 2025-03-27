package com.example.crud.service;

import com.example.crud.dao.EmergencyContactRepository;
import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmergencyContactServiceImpl implements EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;

    public EmergencyContactServiceImpl(EmergencyContactRepository emergencyContactRepository) {
        this.emergencyContactRepository = emergencyContactRepository;
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

    @Cacheable(value = "contact", key = "#id")
    @Override
    public EmergencyContact findById(int id) {
        return emergencyContactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found!"));
    }
}