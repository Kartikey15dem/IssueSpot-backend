package com.example.issuespot.domain.entities;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
@Entity
@Table(name = "post_acks")
@Getter @Setter @NoArgsConstructor
public class PostAck {
  @EmbeddedId
  private PostAckId id;
  @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
