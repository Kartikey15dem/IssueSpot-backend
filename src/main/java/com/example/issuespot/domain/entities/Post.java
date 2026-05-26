package com.example.issuespot.domain.entities;
import com.example.issuespot.domain.enums.MediaType;
import com.example.issuespot.domain.enums.PostLevel;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;
@Entity @Table(name = "posts")
@Getter @Setter @NoArgsConstructor
public class Post {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "user_id", nullable = false) private UUID userId;
  @Enumerated(EnumType.STRING) @Column(name = "post_level", nullable = false) private PostLevel postLevel;
  @Column(name = "post_text", nullable = false) private String postText;
  @Enumerated(EnumType.STRING) @Column(name = "media_type", nullable = false) private MediaType mediaType;
  @Column(name = "media_url") private String mediaUrl;
  @Column(nullable = false) private int likes;
  @Column(nullable = false) private int comments;
  private String locality;
  private String district;
  private String state;
  private String country;
  @Column(columnDefinition = "geography(Point, 4326)") private Point coordinates;
  @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
  @UpdateTimestamp @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
