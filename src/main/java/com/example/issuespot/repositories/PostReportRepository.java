package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.PostReport;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PostReportRepository extends JpaRepository<PostReport, UUID> {
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);
}
