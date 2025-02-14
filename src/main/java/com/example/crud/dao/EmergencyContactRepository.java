package com.example.crud.dao;

import com.example.crud.entity.EmergencyContact;
import com.example.crud.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Integer> {
    List<EmergencyContact> findAllByUserId(int userId);

        boolean existsByPhoneAndUser(String phone, User user);

}
