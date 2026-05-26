package com.example.issuespot.services;
import com.example.issuespot.domain.dtos.*;
import java.util.UUID;
public interface ProfileService {
    ProfileDto getProfile(UUID userId);
    ProfileDto upsertProfile(UUID userId, UpsertProfileRequest request);
}
