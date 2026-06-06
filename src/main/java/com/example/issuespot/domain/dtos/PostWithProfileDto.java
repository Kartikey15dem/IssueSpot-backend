package com.example.issuespot.domain.dtos;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
public record PostWithProfileDto(
    UUID id, UUID userId, String postLevel, String postText, String mediaType, List<String> mediaUrls,
    int likes, int comments, Instant createdAt, ProfileInfoDto profiles,
    String locality, String district, String state, String country, CoordinatesDto coordinates,
    boolean isLiked, boolean isReported
) {}
