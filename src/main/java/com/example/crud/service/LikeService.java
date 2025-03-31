package com.example.crud.service;

import com.example.crud.entity.Likes;
import com.example.crud.dao.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Cacheable(value = "likeCount", key = "#postId")
    public long countLikesByPostId(int postId) {
        return likeRepository.countByPostId(postId);
    }

    @Cacheable(value = "userLikeStatus", key = "#postId + '-' + #userId")
    public boolean hasUserLikedPost(int postId, int userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }

    @Caching(evict = {
            @CacheEvict(value = "likeCount", key = "#result.post.id"), // Evict count using post ID from saved like
            @CacheEvict(value = "userLikeStatus", key = "#result.post.id + '-' + #result.user.id") // Evict status using post/user IDs
    })
    public Likes addLike(Likes like) {

        Optional<Likes> existingLike = likeRepository.findByPostIdAndUserId(
                like.getPost().getId(),
                like.getUser().getId()
        );

        if (existingLike.isPresent()) {
            return existingLike.get();
        }

        return likeRepository.save(like);
    }

    @Caching(evict = {
            @CacheEvict(value = "likeCount", key = "#postId"),
            @CacheEvict(value = "userLikeStatus", key = "#postId + '-' + #userId")
    })
    public void removeLike(int postId, int userId) {
        Optional<Likes> like = likeRepository.findByPostIdAndUserId(postId, userId);
        like.ifPresent(l -> likeRepository.deleteById(l.getId()));
    }
}
