package com.example.crud.service;

import com.example.crud.entity.Comments;
import com.example.crud.dao.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Cacheable(value = "postComments", key = "#postId")
    public List<Comments> getCommentsByPostId(int postId) {
        return commentRepository.findByPostId(postId);
    }

    @Cacheable(value = "userComments", key = "#userId")
    public List<Comments> getCommentsByUserId(int userId) {
        return commentRepository.findByUserId(userId);
    }

    @Caching(evict = {
            @CacheEvict(value = "postComments", key = "#result.post.id"), // Evict post comments list using post ID
            @CacheEvict(value = "userComments", key = "#result.user.id")  // Evict user comments list using user ID
    })
    public Comments addComment(Comments comment) {
        return commentRepository.save(comment);
    }

    @Caching(evict = {
            @CacheEvict(value = "postComments", allEntries = true),
            @CacheEvict(value = "userComments", allEntries = true)
    })
    public void deleteComment(int id) {
        commentRepository.deleteById(id);
    }
}
