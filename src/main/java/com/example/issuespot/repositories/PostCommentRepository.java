package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.PostComment;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
  Page<PostComment> findByPostIdOrderByCreatedAtDesc(UUID postId, Pageable pageable);
}
