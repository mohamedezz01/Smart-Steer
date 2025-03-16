package com.example.crud.dao;

import com.example.crud.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Integer> {
    List<Comments> findByPostId(int postId);
    List<Comments> findByUserId(int userId);
}
