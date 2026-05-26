package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.PostAck;
import com.example.issuespot.domain.entities.PostAckId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface PostAckRepository extends JpaRepository<PostAck, PostAckId> {
  @Query("select a.id.postId from PostAck a where a.id.userId = :userId")
  List<UUID> findPostIdsLikedByUser(@Param("userId") UUID userId);
}
