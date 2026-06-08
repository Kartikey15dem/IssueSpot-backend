package com.example.issuespot.controllers;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.services.PostService;
import com.example.issuespot.services.ProfileService;
import com.example.issuespot.util.SecurityUtil;
import com.example.issuespot.exceptions.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/profile") @RequiredArgsConstructor
public class ProfileController {
  private final ProfileService profileService;
  private final PostService postService;
  @GetMapping("/me") public ProfileDto getMyProfile() {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    return profileService.getProfile(userId);
  }
  @PutMapping(value = "/me", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE) public ProfileDto updateMyProfile(@RequestPart("profile") @Valid UpsertProfileRequest request, @RequestPart(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    return profileService.upsertProfile(userId, request, file);
  }
  
  @PostMapping("/me/email-change/request")
  public void requestEmailChange(@Valid @RequestBody EmailChangeRequest request) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    profileService.requestEmailChange(userId, request.newEmail());
  }

  @PostMapping("/me/email-change/verify")
  public void verifyEmailChange(@Valid @RequestBody EmailChangeVerifyRequest request) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    profileService.verifyEmailChange(userId, request.newEmail(), request.code());
  }

  @GetMapping("/me/liked-posts") public PagedResponse<PostWithProfileDto> getMyLikedPosts(@RequestParam(defaultValue="LATEST") String sort, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int limit) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    return postService.getPostsLikedByUser(userId, sort, page, limit);
  }

  @GetMapping("/me/posts") public PagedResponse<PostWithProfileDto> getMyPosts(@RequestParam(defaultValue="LATEST") String sort, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int limit) {
    UUID userId = SecurityUtil.currentUserId().orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    return postService.getPostsByUser(userId, sort, page, limit);
  }
}
