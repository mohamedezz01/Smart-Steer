package com.example.crud.service;

import com.example.crud.entity.Comments;
import com.example.crud.dao.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comments> getCommentsByPostId(int postId) {
        return commentRepository.findByPostId(postId);
    }

    public List<Comments> getCommentsByUserId(int userId) {
        return commentRepository.findByUserId(userId);
    }

    public Comments addComment(Comments comment) {
        return commentRepository.save(comment);
    }

    public void deleteComment(int id) {
        commentRepository.deleteById(id);
    }
}
