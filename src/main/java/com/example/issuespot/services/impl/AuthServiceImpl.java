package com.example.issuespot.services.impl;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.domain.entities.*;
import com.example.issuespot.exceptions.BadRequestException;
import com.example.issuespot.repositories.*;
import com.example.issuespot.services.AuthService;
import com.example.issuespot.services.EmailProvider;
import com.example.issuespot.util.CryptoUtil;
import com.example.issuespot.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AppUserRepository appUserRepository;
    private final AuthOtpCodeRepository authOtpCodeRepository;
    private final ProfileRepository profileRepository;
    private final JwtUtil jwtUtil;
    private final EmailProvider emailProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${issuespot.auth.otp.ttl-seconds}") private long otpTtlSeconds;
    @Value("${issuespot.auth.otp.dev-return-code}") private boolean devReturnCode;
    @Value("${issuespot.jwt.secret}") private String otpSaltSecret;

    @Override @Transactional
    public AuthRequestOtpResponse requestOtp(String email) {
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        AuthOtpCode otp = new AuthOtpCode();
        otp.setEmail(email);
        otp.setCodeHash(CryptoUtil.sha256Base64(email + ":" + code + ":" + otpSaltSecret));
        otp.setExpiresAt(Instant.now().plusSeconds(otpTtlSeconds));
        authOtpCodeRepository.save(otp);
        emailProvider.sendOtp(email, code);
        return new AuthRequestOtpResponse(true, devReturnCode ? code : null);
    }

    @Override @Transactional
    public AuthVerifyOtpResponse verifyOtp(String email, String code) {
        verifyOtpOnly(email, code);

        AppUser user = appUserRepository.findByEmail(email).orElseGet(() -> {
            AppUser created = new AppUser();
            created.setEmail(email);
            return appUserRepository.save(created);
        });

        boolean isNewUser = !profileRepository.existsById(user.getId());
        String token = jwtUtil.issueToken(user.getId(), user.getEmail());
        return new AuthVerifyOtpResponse(token, isNewUser);
    }

    @Override @Transactional
    public void verifyOtpOnly(String email, String code) {
        AuthOtpCode latest = authOtpCodeRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException("No OTP requested"));
        if (latest.getConsumedAt() != null) throw new BadRequestException("OTP already used");
        if (latest.getExpiresAt().isBefore(Instant.now())) throw new BadRequestException("OTP expired");
        if (!latest.getCodeHash().equals(CryptoUtil.sha256Base64(email + ":" + code + ":" + otpSaltSecret))) throw new BadRequestException("Invalid OTP");
        
        latest.setConsumedAt(Instant.now());
        authOtpCodeRepository.save(latest);
    }
}
