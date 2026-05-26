package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.PostComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
  List<PostComment> findByPostIdOrderByCreatedAtDesc(UUID postId);
}
