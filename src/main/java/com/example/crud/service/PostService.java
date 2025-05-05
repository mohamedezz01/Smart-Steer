package com.example.crud.service;

import com.example.crud.dto.PostResponse;
import com.example.crud.entity.Likes;
import com.example.crud.entity.Posts;
import com.example.crud.dao.PostRepository;
import com.example.crud.dao.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Cacheable(value = "allPostsDTO", key = "#userId")
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPostsAsDTO(int userId) {
        List<Posts> posts = postRepository.findAll();

        return posts.stream().map(post -> {
            PostResponse dto = new PostResponse();

            dto.setId(post.getId());
            dto.setContent(post.getContent());
          //  dto.setCreatedAt(post.getCreatedAt());
            dto.setImageUrl(post.getImageUrl());

            if (post.getAdmin() != null) {
                dto.setUserName(post.getAdmin().getUsername());
             //   dto.setProfilePic(post.getAdmin().getProfilePicture());
                String baseUrl = "https://smart-steer-production.up.railway.app";
                dto.setProfilePictureUrl(baseUrl + "/profilePicture?userId=" + post.getAdmin().getId());
            }

            List<Likes> likesForPost = post.getLikes();
            dto.setLikeCount(likesForPost != null ? likesForPost.size() : 0);

            List<String> likedByUsernames = likesForPost != null ?
                    likesForPost.stream()
                            .filter(like -> like.getUser() != null)
                            .map(like -> like.getUser().getUsername())
                            .collect(Collectors.toList()) : Collections.emptyList();
            dto.setLikedByUsernames(likedByUsernames);

            boolean isLikedByUser = likeRepository.findByPostIdAndUserId(post.getId(), userId).isPresent();
            dto.setPostLikedByUser(isLikedByUser);

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Posts> getPostById(int id) {
        return postRepository.findById(id);
    }

    @Cacheable(value = "adminPosts", key = "#adminId")
    @Transactional(readOnly = true)
    public List<Posts> getPostsByAdminId(int adminId) {
        return postRepository.findByAdminId(adminId);
    }

    @CacheEvict(value = {"post", "allPosts", "adminPosts", "allPostsDTO"}, allEntries = true)
    @Transactional
    public void deletePost(int id) {
        postRepository.deleteById(id);
    }

    @CacheEvict(value = {"allPosts", "adminPosts", "allPostsDTO"}, allEntries = true)
    @Transactional
    public Posts createPost(Posts post) {
        return postRepository.save(post);
    }
}
