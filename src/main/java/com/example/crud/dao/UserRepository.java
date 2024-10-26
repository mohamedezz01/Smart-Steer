package com.example.crud.dao;

import com.example.crud.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
    User findByEmail(String email);

    User findByVerificationCode(String verificationCode);
    User findByUsername(String username);
}
