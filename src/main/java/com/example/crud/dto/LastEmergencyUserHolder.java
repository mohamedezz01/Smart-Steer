package com.example.crud.dto;

public class LastEmergencyUserHolder {
    private static Integer userId;

    public static synchronized void setUserId(Integer id) {
        userId = id;
    }

    public static synchronized Integer getUserId() {
        return userId;
    }
}
