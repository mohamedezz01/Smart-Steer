package com.example.crud.service;

import com.example.crud.dao.LikeRepository;
import com.example.crud.entity.Likes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CacheManager cacheManager;

    @Transactional
    public Likes addLike(Likes like) {
        int postId = like.getPost().getId();
        int userId = like.getUser().getId();

        Optional<Likes> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);
        if (existingLike.isPresent()) {
            return existingLike.get();
        }

        Likes savedLike = likeRepository.save(like);
        evictLikeAndPostCaches(postId, userId);
        return savedLike;
    }

    @Transactional
    public void removeLike(int postId, int userId) {
        Optional<Likes> like = likeRepository.findByPostIdAndUserId(postId, userId);
        if (like.isPresent()) {
            likeRepository.delete(like.get());
            evictLikeAndPostCaches(postId, userId);
        }
    }

    private void evictLikeAndPostCaches(int postId, int userId) {
        // Evict like-related caches
        if (cacheManager.getCache("likeCount") != null) {
            cacheManager.getCache("likeCount").evict(postId);
        }

        if (cacheManager.getCache("userLikeStatus") != null) {
            cacheManager.getCache("userLikeStatus").evict(postId + "-" + userId);
        }

        // Evict only the current user's post DTO cache
        if (cacheManager.getCache("allPostsDTO") != null) {
            cacheManager.getCache("allPostsDTO").evict(userId);
        }

        // Optional: Evict specific post cache if you have one
        if (cacheManager.getCache("post") != null) {
            cacheManager.getCache("post").evict(postId);
        }

        // Optional: If you also cache all posts or admin posts
        if (cacheManager.getCache("allPosts") != null) {
            cacheManager.getCache("allPosts").evict(postId);
        }
    }
}
