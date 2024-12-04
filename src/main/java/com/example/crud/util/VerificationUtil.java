package com.example.crud.util;

import java.util.Random;

public class VerificationUtil {

    public static String generateVerificationCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); // Generate a random number between 1000 and 9999
        return String.valueOf(code);
    }
}
