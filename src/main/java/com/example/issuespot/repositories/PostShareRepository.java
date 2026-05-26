package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.PostShare;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PostShareRepository extends JpaRepository<PostShare, UUID> {}
