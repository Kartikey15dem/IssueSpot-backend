package com.example.issuespot.domain.entities;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
@Entity
@Table(name = "profiles")
@Getter @Setter @NoArgsConstructor
public class Profile {
  @Id
  private UUID id;
  @Column(nullable = false)
  private String name;
  @Column(nullable = false)
  private String email;
  @Column(name = "image_url")
  private String imageUrl;
  @Column(name = "total_posts", nullable = false)
  private int totalPosts;
  @Column(nullable = false)
  private int acks;
  @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
  @UpdateTimestamp @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
