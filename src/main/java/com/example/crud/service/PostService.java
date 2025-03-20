package com.example.crud.service;

import com.example.crud.entity.Posts;
import com.example.crud.dao.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<Posts> getAllPosts() {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Posts> getPostById(int id) {
        return postRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Posts> getPostsByAdminId(int adminId) {
        return postRepository.findByAdminId(adminId);
    }

    @Transactional
    public Posts createPost(Posts post) {
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(int id) {
        postRepository.deleteById(id);
    }
}