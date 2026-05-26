package com.example.issuespot.controllers;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.services.PostService;
import com.example.issuespot.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/posts") @RequiredArgsConstructor
public class PostController {
  private final PostService postService;
  @GetMapping public PagedResponse<PostWithProfileDto> listPosts(
      @RequestParam(required=false) String level, @RequestParam(required=false) String post_level,
      @RequestParam(required=false) String locality, @RequestParam(required=false) String district,
      @RequestParam(required=false) String state, @RequestParam(required=false) String country,
      @RequestParam(required=false) Double lat, @RequestParam(required=false) Double lon,
      @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int limit) {
    String effective = level != null ? level : post_level;
    return postService.getPostsByLevel(effective, locality, district, state, country, lat, lon, page, limit);
  }
  @PostMapping public PostWithProfileDto createPost(@Valid @RequestBody CreatePostRequest request) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
    return postService.createPost(userId, request);
  }
  @PostMapping("/{postId}/like") public void toggleLike(@PathVariable UUID postId) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
    postService.toggleLike(userId, postId);
  }
}
