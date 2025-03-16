 package com.example.crud.service;

import com.example.crud.entity.Posts;
import com.example.crud.dao.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<Posts> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Posts> getPostById(int id) {
        return postRepository.findById(id);
    }

    public List<Posts> getPostsByAdminId(int adminId) {
        return postRepository.findByAdminId(adminId);
    }

    public Posts createPost(Posts post) {
        return postRepository.save(post);
    }

    public void deletePost(int id) {
        postRepository.deleteById(id);
    }
}
