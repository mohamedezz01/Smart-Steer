package com.example.crud.service;

import com.example.crud.entity.Likes;
import com.example.crud.dao.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    public long countLikesByPostId(int postId) {
        return likeRepository.countByPostId(postId);
    }

    public boolean hasUserLikedPost(int postId, int userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }

    public Likes addLike(Likes like) {
        return likeRepository.save(like);
    }

    public void removeLike(int postId, int userId) {
        Optional<Likes> like = likeRepository.findByPostIdAndUserId(postId, userId);
        like.ifPresent(l -> likeRepository.deleteById(l.getId()));
    }
}
