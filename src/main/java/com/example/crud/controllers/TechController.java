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
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
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
    @Autowired
    private GitHubService gitHubService;

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

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = gitHubService.uploadImageToGitHub(file);
        }

        post.setImageUrl(imageUrl);
        post.setAdmin(user);
        Posts savedPost = postService.createPost(post);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post created successfully");
        response.put("postId", savedPost.getId());
        response.put("content", savedPost.getContent());
        response.put("createdAt", savedPost.getCreatedAt());
        response.put("imageUrl", savedPost.getImageUrl());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/posts")
    public ResponseEntity<?> getAllPosts() {
        List<Posts> posts = postService.getAllPosts();

        List<PostResponse> response = new ArrayList<>();
        for (Posts post : posts) {

            PostResponse postResponse = new PostResponse();
            postResponse.setId(post.getId());
            postResponse.setContent(post.getContent());
            postResponse.setCreatedAt(post.getCreatedAt());

            if (post.getImageUrl() != null) {
                postResponse.setImageUrl(post.getImageUrl());
            }

            response.add(postResponse);
        }

        return ResponseEntity.ok(response);
    }
    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPostById(@PathVariable int id) {
        Optional<Posts> postOptional = postService.getPostById(id);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Posts post = postOptional.get();

        Map<String, Object> response = new HashMap<>();
        response.put("id", post.getId());
        response.put("content", post.getContent());
        response.put("createdAt", post.getCreatedAt());

        if (post.getImageUrl() != null) {
            response.put("imageUrl", post.getImageUrl());
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable int id) {
        Optional<Posts> postOptional = postService.getPostById(id);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Posts post = postOptional.get();

        if (post.getImageUrl() != null) {
            try {
                deleteImageFromGitHub(post.getImageUrl());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete image from GitHub");
            }
        }

        // Delete the post from the database
        postService.deletePost(id);

        return ResponseEntity.ok("Post deleted successfully");
    }
    private void deleteImageFromGitHub(String imageUrl) throws IOException {
        // Extract file path from URL
        String baseUrl = "https://raw.githubusercontent.com/mohamedezz01/Smart-Steer/ImageStore/";
        String filePath = imageUrl.replace(baseUrl, "");

        System.out.println("File Path: " + filePath);

        // Include branch reference in API URL
        String apiUrl = "https://api.github.com/repos/mohamedezz01/Smart-Steer/contents/" + filePath + "?ref=ImageStore";
        System.out.println("API URL: " + apiUrl);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String githubToken = System.getenv("GITHUB_TOKEN");
        headers.set("Authorization", "token "+githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        ResponseEntity<Map> getResponse;
        try {
            getResponse = restTemplate.exchange(apiUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("File not found: " + filePath);
            throw new IOException("File not found: " + filePath);
        }

        if (getResponse.getStatusCode() != HttpStatus.OK) {
            throw new IOException("Failed to fetch file details from GitHub");
        }

        String sha = (String) getResponse.getBody().get("sha");
        System.out.println("File SHA: " + sha);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", "Delete image: " + filePath);
        requestBody.put("sha", sha);
        requestBody.put("branch", "ImageStore"); // Include branch in request body

        ResponseEntity<Map> deleteResponse = restTemplate.exchange(
                apiUrl,
                HttpMethod.DELETE,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );

        if (deleteResponse.getStatusCode() != HttpStatus.OK) {
            throw new IOException("Failed to delete image from GitHub");
        }
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