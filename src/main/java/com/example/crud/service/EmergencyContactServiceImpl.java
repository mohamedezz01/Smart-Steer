package com.example.crud.service;

import com.example.crud.dao.EmergencyContactRepository;
import com.example.crud.entity.EmergencyContact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmergencyContactServiceImpl implements EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;

    public EmergencyContactServiceImpl(EmergencyContactRepository emergencyContactRepository) {
        this.emergencyContactRepository = emergencyContactRepository;
    }

    @Override
    public List<EmergencyContact> getContactsByUserId(int userId) {
        return emergencyContactRepository.findAllByUserId(userId);
    }

    @Override
    public EmergencyContact addContact(EmergencyContact contact) {
        return emergencyContactRepository.save(contact);
    }

    @Override
    public void deleteContact(int contactId) {
        emergencyContactRepository.deleteById(contactId);
    }
    @Override
    public EmergencyContact findById(int id) {
        return emergencyContactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found!"));
    }

}
