package com.example.crud.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class PostResponse {
    private int id;
    private String content;
    private Date createdAt;
    private String imageUrl;
    private String userName;
    private byte[] profilePic;
    private String profilePictureUrl;
    private int likeCount;
    private List<String> likedByUsernames;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public byte[] getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(byte[] profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public List<String> getLikedByUsernames() {
        return likedByUsernames;
    }

    public void setLikedByUsernames(List<String> likedByUsernames) {
        this.likedByUsernames = likedByUsernames;
    }
}