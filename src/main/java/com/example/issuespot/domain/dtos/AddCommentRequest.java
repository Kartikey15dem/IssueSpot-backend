package com.example.issuespot.domain.dtos;
import jakarta.validation.constraints.NotBlank;
public record AddCommentRequest(@NotBlank String comment) {}
