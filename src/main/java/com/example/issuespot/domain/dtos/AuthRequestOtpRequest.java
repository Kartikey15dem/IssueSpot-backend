package com.example.issuespot.domain.dtos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record AuthRequestOtpRequest(@NotBlank @Email String email) {}
