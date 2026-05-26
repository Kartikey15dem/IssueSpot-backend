package com.example.issuespot.controllers;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  @PostMapping("/otp/request")
  public AuthRequestOtpResponse requestOtp(@Valid @RequestBody AuthRequestOtpRequest request) { return authService.requestOtp(request.email()); }
  @PostMapping("/otp/verify")
  public AuthVerifyOtpResponse verifyOtp(@Valid @RequestBody AuthVerifyOtpRequest request) { return authService.verifyOtp(request.email(), request.otp()); }
}
