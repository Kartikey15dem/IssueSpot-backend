package com.example.issuespot.domain.entities;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
@Entity
@Table(name = "auth_otp_codes")
@Getter @Setter @NoArgsConstructor
public class AuthOtpCode {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(nullable = false)
  private String email;
  @Column(name = "code_hash", nullable = false)
  private String codeHash;
  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;
  @Column(name = "consumed_at")
  private Instant consumedAt;
  @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
