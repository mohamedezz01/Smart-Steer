package com.example.crud.dto;

public class UserDTO {
    private String username;
    private String email;
    private byte[] profilePicture;

    public UserDTO(String username, String email, byte[] profilePicture) {
        this.username = username;
        this.email = email;
        this.profilePicture = profilePicture;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
}