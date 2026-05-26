package com.example.issuespot.util;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
public final class SecurityUtil {
  private SecurityUtil() {}
  public static Optional<UUID> currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) return Optional.empty();
    if (!(auth.getPrincipal() instanceof Jwt jwt)) return Optional.empty();
    try { return Optional.of(UUID.fromString(jwt.getSubject())); } 
    catch (IllegalArgumentException e) { return Optional.empty(); }
  }
}
