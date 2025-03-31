package com.example.crud.service;

import com.example.crud.dto.PostResponse;
import com.example.crud.entity.Likes;
import com.example.crud.entity.Posts;
import com.example.crud.dao.PostRepository;
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

    @Cacheable(value = "allPostsDTO")
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPostsAsDTO() {
        List<Posts> posts = postRepository.findAll();

        return posts.stream().map(post -> {
            PostResponse dto = new PostResponse();

            dto.setId(post.getId());
            dto.setContent(post.getContent());
            dto.setCreatedAt(post.getCreatedAt());
            dto.setImageUrl(post.getImageUrl());

            if (post.getAdmin() != null) {
                dto.setUserName(post.getAdmin().getUsername());
                dto.setProfilePic(post.getAdmin().getProfilePicture());
                dto.setProfilePictureUrl("/profilePicture?userId=" + post.getAdmin().getId());
            }

            List<Likes> likesForPost = post.getLikes();
            dto.setLikeCount(likesForPost != null ? likesForPost.size() : 0);
            List<String> likedByUsernames = likesForPost != null ?
                    likesForPost.stream()
                            .filter(like -> like.getUser() != null) //null checks
                            .map(like -> like.getUser().getUsername())
                            .collect(Collectors.toList()) : Collections.emptyList();
            dto.setLikedByUsernames(likedByUsernames);

            // Option B: Using LikeService (more reliable if EAGER causes issues, may hit cache)
            // dto.setLikeCount((int) likeService.countLikesByPostId(post.getId()));
            // // Getting liked usernames via service might require a new LikeService method
            // dto.setLikedByUsernames(likeService.getLikerUsernamesForPost(post.getId())); // Example method

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

    @CacheEvict(value = {"allPosts", "adminPosts"}, allEntries = true)
    @Transactional
    public Posts createPost(Posts post) {
        return postRepository.save(post);
    }

    @CacheEvict(value = {"post", "allPosts", "adminPosts"}, allEntries = true, key = "#id")
    @Transactional
    public void deletePost(int id) {
        postRepository.deleteById(id);
    }
}