package com.example.issuespot.domain.dtos;
import jakarta.validation.constraints.NotBlank;
public record UpsertProfileRequest(@NotBlank String name, @NotBlank String email, String imageUrl) {}
