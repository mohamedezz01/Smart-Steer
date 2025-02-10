package com.example.crud.service;

import com.example.crud.entity.EmergencyContact;

import java.util.Date;
import java.util.List;

public interface EmergencyContactService {
    List<EmergencyContact> getContactsByUserId(int userId);

    EmergencyContact addContact(EmergencyContact contact);

    EmergencyContact findById(int id);
    void deleteContact(int contactId);
}
