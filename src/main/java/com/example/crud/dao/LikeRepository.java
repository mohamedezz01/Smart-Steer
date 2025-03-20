package com.example.crud.dao;

import com.example.crud.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Likes, Integer> {
    long countByPostId(int postId);
    Optional<Likes> findByPostIdAndUserId(int postId, int userId);

}
