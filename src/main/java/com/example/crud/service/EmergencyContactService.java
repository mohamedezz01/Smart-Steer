package com.example.crud.service;

import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;

import java.util.Date;
import java.util.List;

public interface EmergencyContactService {
    List<EmergencyContact> getContactsByUserId(int userId);

    EmergencyContact addContact(EmergencyContact contact);
    public boolean existsByPhoneAndUser(String phone, User user);
    EmergencyContact findById(int id);
    void deleteContact(int contactId);
    public void notifyUserIfEmailExists(String email, String addedByName, String addedByPhone);
    public boolean existsByEmailAndUser(String email, User user);
}
