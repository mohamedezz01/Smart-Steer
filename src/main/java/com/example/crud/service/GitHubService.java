package com.example.crud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class GitHubService {

    @Value("${github.token}")
    private String githubToken;
    public String uploadImageToGitHub(MultipartFile file) throws IOException {
        String repoOwner = "mohamedezz01";
        String repoName = "Smart-Steer";
        String branch = "ImageStore";

        String filePath = "images/" + file.getOriginalFilename();

        String apiUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/contents/" + filePath + "?ref=" + branch;
        // Debugging: Print the API URL
        System.out.println("API URL: " + apiUrl);
        if (githubToken != null && !githubToken.isEmpty()) {
            System.out.println("GitHubService: githubToken is loaded. Length: " + githubToken.length());
        } else {
            System.out.println("GitHubService: githubToken is NULL or EMPTY after injection from Secret Manager!");
        }
        // Fetch the file details to get the SHA (if the file exists)
        String sha = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + githubToken);
            headers.set("Accept", "application/vnd.github.v3+json");

            ResponseEntity<Map> getResponse = restTemplate.exchange(apiUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            if (getResponse.getStatusCode() == HttpStatus.OK) {
                sha = (String) getResponse.getBody().get("sha");
                System.out.println("File SHA: " + sha);
            }
        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("File does not exist: " + filePath);
        }

        // Encode the file content as Base64
        String fileContent = Base64.getEncoder().encodeToString(file.getBytes());

        // Create the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", "Upload image: " + file.getOriginalFilename());
        requestBody.put("content", fileContent);
        requestBody.put("branch", branch);

        // Include the SHA if the file already exists (to update it)
        if (sha != null) {
            requestBody.put("sha", sha);
        }

        // Send the request to GitHub API
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.PUT, request, Map.class);

            // Debugging: Print the response status and body
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                // Return the direct link to the file
                return "https://raw.githubusercontent.com/" + repoOwner + "/" + repoName + "/" + branch + "/" + filePath;
            } else {
                throw new IOException("Failed to upload image to GitHub");
            }
        } catch (HttpClientErrorException e) {
            // Debugging: Print the error response
            System.out.println("Error Response: " + e.getResponseBodyAsString());
            throw new IOException("Failed to upload image to GitHub: " + e.getMessage(), e);
        }
    }

}