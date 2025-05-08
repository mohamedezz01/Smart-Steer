package com.example.crud.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_KEY = "e2a5636c74ae5df09d0ea3e79d965034cbac5ac8";
 //   private final String SMS_URL = "https://app.sms8.io/services/sendFront.php?key=e2a5636c74ae5df09d0ea3e79d965034cbac5ac8";

    public boolean sendSms(String to, String messageText) {
        String SEND_URL = "https://app.sms8.io/services/send.php";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("number", to);
        body.add("message", messageText);
        body.add("key", API_KEY);
        body.add("devices", "0"); // Use 0 or specific device ID
        body.add("type", "sms");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            System.out.println("📤 Sending SMS to: " + to);
            System.out.println("📨 Message: " + messageText);
            ResponseEntity<String> response = restTemplate.postForEntity(SEND_URL, request, String.class);
            System.out.println("✅ SMS API Response Status: " + response.getStatusCode());
            System.out.println("📄 SMS API Response Body: " + response.getBody());

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            System.out.println("❌ SMS sending failed:");
            ex.printStackTrace();
            return false;
        }
    }


}
