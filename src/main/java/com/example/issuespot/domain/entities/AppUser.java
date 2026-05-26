package com.example.issuespot.domain.entities;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
@Entity @Table(name = "app_users")
@Getter @Setter @NoArgsConstructor
public class AppUser {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(nullable = false, unique = true)
  private String email;
  @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
  @UpdateTimestamp @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
