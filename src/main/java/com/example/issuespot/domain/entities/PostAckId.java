package com.example.issuespot.domain.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;
@Embeddable @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PostAckId implements Serializable {
  @Column(name = "user_id", nullable = false)
  private UUID userId;
  @Column(name = "post_id", nullable = false)
  private UUID postId;
  public UUID getUserId() { return userId; }
  public UUID getPostId() { return postId; }
}
