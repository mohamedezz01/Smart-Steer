package com.example.crud.dao;

import com.example.crud.entity.SerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SerialNumberRepository extends JpaRepository<SerialNumber, Integer> {
    Optional<SerialNumber> findBySerialNumber(String serialNumber);
}