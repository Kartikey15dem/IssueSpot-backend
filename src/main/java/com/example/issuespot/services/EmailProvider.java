package com.example.issuespot.services;
public interface EmailProvider {
    void sendOtp(String toEmail, String otpCode);
}
