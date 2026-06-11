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
    
    /* WHY THESE SPECIFIC CLAIMS:
     * We encode the UUID directly into the 'subject' (sub) claim.
     * This allows the `SecurityUtil.currentUserId()` helper to extract the UUID 
     * synchronously from the Request Context without needing a database lookup 
     * on every single authenticated API request, massively boosting performance.
     */
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
