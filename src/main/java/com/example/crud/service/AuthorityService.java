package com.example.crud.service;

import com.example.crud.dao.AuthorityRepository;
import com.example.crud.entity.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorityService implements AuthServ {

    @Autowired
    private AuthorityRepository authorityRepository;


    @Override
    public Authority save(Authority authority) {
       return authorityRepository.save(authority);
    }
}
