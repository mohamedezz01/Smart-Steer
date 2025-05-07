package com.example.crud.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_KEY = "zai8rap4t66x47nsq33ox3aj9sgettjx";
    private final String SMS_URL = "https://restapi.easysendsms.app/v1/rest/sms/send";

    public boolean sendSms(String to, String messageText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", API_KEY);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = new HashMap<>();
        body.put("from", "SmartSteer");
        body.put("to", to);
        body.put("text", messageText);
        body.put("type", "0");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            System.out.println("📤 Sending SMS to: " + to);
            System.out.println("📨 Message: " + messageText);
            ResponseEntity<String> response = restTemplate.postForEntity(SMS_URL, request, String.class);
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
