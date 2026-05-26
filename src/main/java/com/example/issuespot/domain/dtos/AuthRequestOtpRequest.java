package com.example.issuespot.domain.dtos;
import jakarta.validation.constraints.*;
public record AuthRequestOtpRequest(@NotBlank @Email String email) {}
