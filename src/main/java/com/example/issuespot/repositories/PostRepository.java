package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.Post;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.issuespot.domain.enums.PostLevel;
public interface PostRepository extends JpaRepository<Post, UUID> {
  Page<Post> findByPostLevelOrderByCreatedAtDesc(com.example.issuespot.domain.enums.PostLevel postLevel, Pageable pageable);
  Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
  @Query("SELECT p FROM Post p JOIN PostAck a ON p.id = a.id.postId WHERE a.id.userId = :userId ORDER BY a.createdAt DESC")
  Page<Post> findLikedPostsByUser(@Param("userId") UUID userId, Pageable pageable);
  Page<Post> findByPostLevelAndLocalityOrderByCreatedAtDesc(com.example.issuespot.domain.enums.PostLevel postLevel, String locality, Pageable pageable);
  Page<Post> findByPostLevelAndDistrictOrderByCreatedAtDesc(com.example.issuespot.domain.enums.PostLevel postLevel, String district, Pageable pageable);
  Page<Post> findByPostLevelAndStateOrderByCreatedAtDesc(com.example.issuespot.domain.enums.PostLevel postLevel, String state, Pageable pageable);
  Page<Post> findByPostLevelAndCountryOrderByCreatedAtDesc(com.example.issuespot.domain.enums.PostLevel postLevel, String country, Pageable pageable);
  @Query(value = "SELECT * FROM posts WHERE CAST(post_level AS text) = :#{#postLevel.name()} AND ST_DWithin(coordinates, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :radius) ORDER BY created_at DESC", nativeQuery = true)
  Page<Post> findByPostLevelAndCoordinatesNear(@Param("postLevel") com.example.issuespot.domain.enums.PostLevel postLevel, @Param("lat") double lat, @Param("lon") double lon, @Param("radius") double radiusInMeters, Pageable pageable);
}
