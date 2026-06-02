package com.example.issuespot.util;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class JwtUtil {
  private final JwtEncoder jwtEncoder;
  public String issueToken(UUID userId, String email) {
    Instant now = Instant.now();
    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("issuespot")
        .issuedAt(now)
        .expiresAt(now.plus(3600, ChronoUnit.HOURS))
        .subject(userId.toString())
        .claim("email", email)
        .build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }
}
