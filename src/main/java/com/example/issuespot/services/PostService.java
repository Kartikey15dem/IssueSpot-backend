package com.example.issuespot.services;
import com.example.issuespot.domain.dtos.*;
import java.util.UUID;
public interface PostService {
    PagedResponse<PostWithProfileDto> getPostsByLevel(String level, String locality, String district, String state, String country, Double lat, Double lon, int page, int limit);
    ActiveIssuesDto getActiveIssuesCount(String level);
    PostWithProfileDto createPost(UUID userId, CreatePostRequest request);
    void deletePost(UUID userId, UUID postId);
    void toggleLike(UUID userId, UUID postId);
    void addComment(UUID userId, UUID postId, String comment);
    void reportPost(UUID userId, UUID postId, String reason);
    void sharePost(UUID userId, UUID postId);
    PagedResponse<PostWithProfileDto> getPostsByUser(UUID userId, int page, int limit);
    PagedResponse<PostWithProfileDto> getPostsLikedByUser(UUID userId, int page, int limit);
}
