package com.example.crud.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeEmailRequest {
    private String newEmail;
    private String confirmEmail;
}
