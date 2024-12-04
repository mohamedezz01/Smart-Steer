package com.example.crud.entity;

public class ResetPasswordRequest {


    private String newPassword;
    private String token;


    public ResetPasswordRequest(String newPassword, String token) {
        this.newPassword = newPassword;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
