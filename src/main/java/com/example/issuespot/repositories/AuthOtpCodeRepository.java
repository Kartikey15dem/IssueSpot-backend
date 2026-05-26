package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.AuthOtpCode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuthOtpCodeRepository extends JpaRepository<AuthOtpCode, UUID> {
  Optional<AuthOtpCode> findTopByEmailOrderByCreatedAtDesc(String email);
}
