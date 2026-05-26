package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.PostAck;
import com.example.issuespot.domain.entities.PostAckId;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PostAckRepository extends JpaRepository<PostAck, PostAckId> {}
