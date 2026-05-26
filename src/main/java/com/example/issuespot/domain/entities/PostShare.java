package com.example.issuespot.domain.entities;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
@Entity
@Table(name = "post_shares")
@Getter @Setter @NoArgsConstructor
public class PostShare {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "post_id", nullable = false)
  private UUID postId;
  @Column(name = "user_id", nullable = false)
  private UUID userId;
  @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
