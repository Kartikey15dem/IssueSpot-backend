package com.example.issuespot.domain.dtos;
import java.time.Instant;
import java.util.UUID;
public record PostWithProfileDto(
    UUID id, UUID userId, String postLevel, String postText, String mediaType, String mediaUrl,
    int likes, int comments, Instant createdAt, ProfileInfoDto profiles,
    String locality, String district, String state, String country, CoordinatesDto coordinates
) {}
