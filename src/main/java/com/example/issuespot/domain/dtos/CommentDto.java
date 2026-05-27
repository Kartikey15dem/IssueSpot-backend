package com.example.issuespot.domain.dtos;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID postId,
    String commentText,
    Instant createdAt,
    ProfileInfoDto profile
) {}
