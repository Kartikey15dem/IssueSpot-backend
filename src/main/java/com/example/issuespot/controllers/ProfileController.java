package com.example.issuespot.controllers;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.services.PostService;
import com.example.issuespot.services.ProfileService;
import com.example.issuespot.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/profile") @RequiredArgsConstructor
public class ProfileController {
  private final ProfileService profileService;
  private final PostService postService;
  @GetMapping("/me") public ProfileDto getMyProfile() {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
    return profileService.getProfile(userId);
  }
  @PutMapping("/me") public ProfileDto updateMyProfile(@Valid @RequestBody UpsertProfileRequest request) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
    return profileService.upsertProfile(userId, request);
  }
  @GetMapping("/me/liked-posts") public PagedResponse<PostWithProfileDto> getMyLikedPosts(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int limit) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
    return postService.getPostsLikedByUser(userId, page, limit);
  }

  @GetMapping("/me/posts") public PagedResponse<PostWithProfileDto> getMyPosts(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int limit) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
    return postService.getPostsByUser(userId, page, limit);
  }
}
