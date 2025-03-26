package com.example.crud.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAccountRequest {
    private String password;
    private String Email;

}
