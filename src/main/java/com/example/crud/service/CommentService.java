package com.example.crud.service;

import com.example.crud.entity.Comments;
import com.example.crud.dao.CommentRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private EntityManager entityManager;

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

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "postComments", allEntries = true),
            @CacheEvict(value = "userComments", allEntries = true)
    })
    public void deleteComment(int id, String currentUsername) {
        Optional<Comments> commentOpt = commentRepository.findById(id);

        if (commentOpt.isEmpty()) {
            throw new RuntimeException("Comment not found.");
        }

        Comments comment = commentOpt.get();

        if (!comment.getUserName().equals(currentUsername)) {
            throw new RuntimeException("You can only delete your own comments.");
        }

        commentRepository.delete(comment);
    }


}
