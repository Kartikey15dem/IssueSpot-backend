package com.example.issuespot.services;
import com.example.issuespot.domain.dtos.AuthRequestOtpResponse;
import com.example.issuespot.domain.dtos.AuthVerifyOtpResponse;
import com.example.issuespot.domain.entities.AppUser;
import com.example.issuespot.domain.entities.AuthOtpCode;
import com.example.issuespot.exceptions.BadRequestException;
import com.example.issuespot.repositories.AppUserRepository;
import com.example.issuespot.repositories.AuthOtpCodeRepository;
import com.example.issuespot.repositories.ProfileRepository;
import com.example.issuespot.util.CryptoUtil;
import com.example.issuespot.util.JwtUtil;
import java.security.SecureRandom;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final AuthOtpCodeRepository authOtpCodeRepository;
    private final ProfileRepository profileRepository;
    private final JwtUtil jwtUtil;
    private final EmailProvider emailProvider;
    private final SecureRandom secureRandom = new SecureRandom();
    @Value("${issuespot.auth.otp.ttl-seconds}")
    private long otpTtlSeconds;
    @Value("${issuespot.auth.otp.dev-return-code}")
    private boolean devReturnCode;
    @Value("${issuespot.jwt.secret}")
    private String otpSaltSecret;

    @Transactional
    public AuthRequestOtpResponse requestOtp(String email) {
        String code = generateOtp();
        AuthOtpCode otp = new AuthOtpCode();
        otp.setEmail(email);
        otp.setCodeHash(hashOtp(email, code));
        otp.setExpiresAt(Instant.now().plusSeconds(otpTtlSeconds));
        authOtpCodeRepository.save(otp);
        System.out.println("Attempting to send OTP email to: " + email);
        emailProvider.sendOtp(email, code);
        return new AuthRequestOtpResponse(true, devReturnCode ? code : null);
    }

    @Transactional
    public AuthVerifyOtpResponse verifyOtp(String email, String code) {
        AuthOtpCode latest = authOtpCodeRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException("No OTP requested"));
        if (latest.getConsumedAt() != null) throw new BadRequestException("OTP already used");
        if (latest.getExpiresAt().isBefore(Instant.now())) throw new BadRequestException("OTP expired");
        if (!latest.getCodeHash().equals(hashOtp(email, code))) throw new BadRequestException("Invalid OTP");
        latest.setConsumedAt(Instant.now());
        authOtpCodeRepository.save(latest);

        AppUser user = appUserRepository.findByEmail(email).orElseGet(() -> {
            AppUser created = new AppUser();
            created.setEmail(email);
            return appUserRepository.save(created);
        });

        boolean isNewUser = !profileRepository.existsById(user.getId());
        String token = jwtUtil.issueToken(user.getId(), user.getEmail());
        return new AuthVerifyOtpResponse(token, isNewUser);
    }

    private String hashOtp(String email, String code) {
        return CryptoUtil.sha256Base64(email + ":" + code + ":" + otpSaltSecret);
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
