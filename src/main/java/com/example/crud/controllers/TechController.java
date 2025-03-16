package com.example.crud.controllers;

import com.example.crud.dto.PostResponse;
import com.example.crud.entity.Comments;
import com.example.crud.entity.Likes;
import com.example.crud.entity.Posts;
import com.example.crud.entity.User;
import com.example.crud.service.*;
import com.example.crud.util.JwtUtil;
import com.example.crud.util.VerificationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/GP/tech")
public class TechController {

    @Autowired
    private PostService postService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private CommentService commentService;

    private UserService userService;
    private EmailService emailService;
    private JwtUtil jwtUtil;

    public TechController(UserService theUserService, EmailService emailService, JwtUtil jwtUtil) {
        this.userService = theUserService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    /// Posts ///

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Posts post = new Posts();
        post.setContent(content);

        if (file != null && !file.isEmpty()) {
            post.setImage(file.getBytes());
        }


        post.setAdmin(user);
        Posts savedPost = postService.createPost(post);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post created successfully");
        response.put("postId", savedPost.getId());
        response.put("content", savedPost.getContent());
        response.put("createdAt", savedPost.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getAllPosts() {
        List<Posts> posts = postService.getAllPosts();
        System.out.println("Number of posts fetched: " + posts.size()); //debugging

        List<PostResponse> response = new ArrayList<>();
        for (Posts post : posts) {
            System.out.println("Processing post ID: " + post.getId()); //debugging

            PostResponse postResponse = new PostResponse();
            postResponse.setId(post.getId());
            postResponse.setContent(post.getContent());
            postResponse.setCreatedAt(post.getCreatedAt());

            if (post.getImage() != null) {
                postResponse.setImage(Base64.getEncoder().encodeToString(post.getImage()));
            }

            response.add(postResponse);
        }

        System.out.println("Number of posts in response: " + response.size()); // Debugging
        return ResponseEntity.ok(response);
    }
    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPostById(@PathVariable int id) {
        Optional<Posts> postOptional = postService.getPostById(id);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Posts post = postOptional.get();

        // Construct a custom response
        Map<String, Object> response = new HashMap<>();
        response.put("id", post.getId());
        response.put("content", post.getContent());
        response.put("createdAt", post.getCreatedAt());

        // Optionally include the image (if needed)
        if (post.getImage() != null) {
            response.put("image", Base64.getEncoder().encodeToString(post.getImage()));
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable int id) {
        postService.deletePost(id);
    }

    /// Likes ///
    @PostMapping("/likes")
    public ResponseEntity<?> likePost(@RequestBody Likes like) {
        Likes savedLike = likeService.addLike(like);

        // Construct a custom response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post liked successfully");
        response.put("likeId", savedLike.getId());
        response.put("postId", savedLike.getPost().getId());
        response.put("userId", savedLike.getUser().getId());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/likes/{postId}/{userId}")
    public void unlikePost(@PathVariable int postId, @PathVariable int userId) {
        likeService.removeLike(postId, userId);
    }

    /// Comments ///

    @PostMapping("/comments")
    public ResponseEntity<?> addComment(@RequestBody Comments comment) {
        Comments savedComment = commentService.addComment(comment);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Comment added successfully");
        response.put("commentId", savedComment.getId());
        response.put("comment", savedComment.getCommentText());
        response.put("createdAt", savedComment.getCreatedAt());
        response.put("userId", savedComment.getUser().getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/post/{postId}")
    public ResponseEntity<?> getCommentsByPost(@PathVariable int postId) {
        List<Comments> comments = commentService.getCommentsByPostId(postId);

        // Construct a custom response
        List<Map<String, Object>> response = new ArrayList<>();
        for (Comments comment : comments) {
            Map<String, Object> commentResponse = new HashMap<>();
            commentResponse.put("id", comment.getId());
            commentResponse.put("comment", comment.getCommentText());
            commentResponse.put("createdAt", comment.getCreatedAt());
            commentResponse.put("userId", comment.getUser().getId());

            response.add(commentResponse);
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{id}")
    public void deleteComment(@PathVariable int id) {
        commentService.deleteComment(id);
    }
}