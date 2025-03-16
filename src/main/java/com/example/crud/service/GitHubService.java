package com.example.crud.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GitHubService {

    public String uploadImageToGitHub(MultipartFile file) throws IOException {
        String repoOwner = "myusername";
        String repoName = "my-app-images";
        String branch = "main";
        String filePath = "images/" + file.getOriginalFilename();
        String githubToken = "YOUR_GITHUB_TOKEN";

        // Encode the file content as Base64
        String fileContent = Base64.getEncoder().encodeToString(file.getBytes());

        // Create the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", "Upload image: " + file.getOriginalFilename());
        requestBody.put("content", fileContent);
        requestBody.put("branch", branch);

        // Send the request to GitHub API
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String apiUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/contents/" + filePath;

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.PUT, request, Map.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            // Return the direct link to the file
            return "https://raw.githubusercontent.com/" + repoOwner + "/" + repoName + "/" + branch + "/" + filePath;
        } else {
            throw new IOException("Failed to upload image to GitHub");
        }
    }
}