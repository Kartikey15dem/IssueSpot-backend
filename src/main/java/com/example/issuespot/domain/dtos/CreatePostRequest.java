package com.example.issuespot.domain.dtos;
import jakarta.validation.constraints.NotBlank;
public record CreatePostRequest(
    @NotBlank String postLevel, @NotBlank String postText, @NotBlank String mediaType,
    String mediaUrl, String locality, String district, String state, String country, CoordinatesDto coordinates
) {}
