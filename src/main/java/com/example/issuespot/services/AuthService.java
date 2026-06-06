package com.example.issuespot.services;
import com.example.issuespot.domain.dtos.*;
import java.util.UUID;
public interface AuthService {
    AuthRequestOtpResponse requestOtp(String email);
    AuthVerifyOtpResponse verifyOtp(String email, String code);
    void verifyOtpOnly(String email, String code);
}
