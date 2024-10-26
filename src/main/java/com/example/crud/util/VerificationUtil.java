package com.example.crud.util;

import java.util.Random;

public class VerificationUtil {

    public static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

}
