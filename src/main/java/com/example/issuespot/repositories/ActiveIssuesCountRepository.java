package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.ActiveIssuesCount;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ActiveIssuesCountRepository extends JpaRepository<ActiveIssuesCount, String> {}
