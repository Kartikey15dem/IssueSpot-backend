package com.example.issuespot.domain.dtos;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
public record CreatePostRequest(
    @NotBlank String postLevel, @NotBlank String postText, @NotBlank String mediaType,
    List<String> mediaUrls, String locality, String district, String state, String country, CoordinatesDto coordinates
) {}
