package com.example.crud.service;

import com.example.crud.entity.Likes;
import com.example.crud.dao.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CacheManager cacheManager;

    @Cacheable(value = "likeCount", key = "#postId")
    public long countLikesByPostId(int postId) {
        return likeRepository.countByPostId(postId);
    }

    @Cacheable(value = "userLikeStatus", key = "#postId + '-' + #userId")
    public boolean hasUserLikedPost(int postId, int userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }

    public Likes addLike(Likes like) {
        int postId = like.getPost().getId();
        int userId = like.getUser().getId();

        Optional<Likes> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);
        if (existingLike.isPresent()) {
            return existingLike.get();
        }

        Likes savedLike = likeRepository.save(like);
        evictLikeCaches(postId, userId);
        return savedLike;
    }

    public void removeLike(int postId, int userId) {
        Optional<Likes> like = likeRepository.findByPostIdAndUserId(postId, userId);
        if (like.isPresent()) {
            likeRepository.delete(like.get());
            evictLikeCaches(postId, userId);
        }
    }

    private void evictLikeCaches(int postId, int userId) {
        if (cacheManager.getCache("likeCount") != null) {
            cacheManager.getCache("likeCount").evict(postId);
        }
        if (cacheManager.getCache("userLikeStatus") != null) {
            cacheManager.getCache("userLikeStatus").evict(postId + "-" + userId);
        }
    }
}
