package com.example.crud.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private int id;
    private String content;
 //   private Date createdAt;
    private String imageUrl;
    private String userName;
   // private byte[] profilePic;
    private String profilePictureUrl;
    private int likeCount;
    private List<String> likedByUsernames;
    private boolean isPostLikedByUser;
}