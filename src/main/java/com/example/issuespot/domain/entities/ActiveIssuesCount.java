package com.example.issuespot.domain.entities;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
@Entity
@Table(name = "active_issues_count")
@Getter @Setter @NoArgsConstructor
public class ActiveIssuesCount {
  @Id
  private String level;
  @Column(name = "total_active_issues", nullable = false)
  private int totalActiveIssues;
  @UpdateTimestamp @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
