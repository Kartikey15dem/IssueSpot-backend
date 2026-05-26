package com.example.issuespot.domain.dtos;
import jakarta.validation.constraints.*;
public record AuthVerifyOtpRequest(@NotBlank @Email String email, @NotBlank @Size(min=6,max=6) String otp) {}
