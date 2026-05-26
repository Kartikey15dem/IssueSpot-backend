package com.example.issuespot.controllers;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.exceptions.BadRequestException;
import com.example.issuespot.services.PostService;
import com.example.issuespot.util.SecurityUtil;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
  private final PostService postService;

  @GetMapping
  public PagedResponse<PostWithProfileDto> listPosts(
      @RequestParam(value = "level", required = false) String level,
      @RequestParam(value = "post_level", required = false) String postLevel,
      @RequestParam(required = false) String locality,
      @RequestParam(required = false) String district,
      @RequestParam(required = false) String state,
      @RequestParam(required = false) String country,
      @RequestParam(required = false) Double lat,
      @RequestParam(required = false) Double lon,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int limit) {
    String effective = level != null ? level : postLevel;
    return postService.getPostsByLevel(effective, locality, district, state, country, lat, lon, page, limit);
  }

  @PostMapping
  public PostWithProfileDto createPost(@Valid @RequestBody CreatePostRequest request) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new BadRequestException("Unauthorized"));
    return postService.createPost(userId, request);
  }

  @DeleteMapping("/{postId}")
  public void deletePost(@PathVariable UUID postId) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new BadRequestException("Unauthorized"));
    postService.deletePost(userId, postId);
  }

  @PostMapping("/{postId}/like")
  public void toggleLike(@PathVariable UUID postId) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new BadRequestException("Unauthorized"));
    postService.toggleLike(userId, postId);
  }

  @PostMapping("/{postId}/comments")
  public void addComment(@PathVariable UUID postId, @Valid @RequestBody AddCommentRequest request) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new BadRequestException("Unauthorized"));
    postService.addComment(userId, postId, request.comment());
  }

  @PostMapping("/{postId}/report")
  public void reportPost(@PathVariable UUID postId, @RequestBody ReportPostRequest request) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new BadRequestException("Unauthorized"));
    postService.reportPost(userId, postId, request != null ? request.reason() : null);
  }

  @PostMapping("/{postId}/share")
  public void sharePost(@PathVariable UUID postId) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new BadRequestException("Unauthorized"));
    postService.sharePost(userId, postId);
  }
}
