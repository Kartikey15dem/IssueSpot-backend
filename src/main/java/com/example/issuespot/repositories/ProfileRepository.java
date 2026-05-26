package com.example.issuespot.repositories;
import com.example.issuespot.domain.entities.Profile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProfileRepository extends JpaRepository<Profile, UUID> {}
