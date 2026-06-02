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
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import java.util.List;
@Entity @Table(name = "posts")
@Getter @Setter @NoArgsConstructor
public class Post {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "user_id", nullable = false) private UUID userId;
   @Column(name = "post_level", nullable = false) private PostLevel postLevel;
  @Column(name = "post_text", nullable = false) private String postText;
   @Column(name = "media_type") private MediaType mediaType;
  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "media_urls", columnDefinition = "text[]") private List<String> mediaUrls;
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
