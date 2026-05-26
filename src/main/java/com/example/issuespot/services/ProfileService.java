package com.example.issuespot.services;
import com.example.issuespot.domain.dtos.ProfileDto;
import com.example.issuespot.domain.dtos.UpsertProfileRequest;
import com.example.issuespot.domain.entities.Profile;
import com.example.issuespot.exceptions.NotFoundException;
import com.example.issuespot.mappers.ProfileMapper;
import com.example.issuespot.repositories.ProfileRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {
  private final ProfileRepository profileRepository;
  private final ProfileMapper profileMapper;

  @Transactional(readOnly = true)
  public ProfileDto getProfile(UUID userId) {
    Profile profile = profileRepository.findById(userId).orElseThrow(() -> new NotFoundException("Profile not found"));
    return profileMapper.toDto(profile);
  }

  @Transactional
  public ProfileDto upsertProfile(UUID userId, UpsertProfileRequest request) {
    Profile profile = profileRepository.findById(userId).orElseGet(() -> {
      Profile p = new Profile();
      p.setId(userId);
      p.setTotalPosts(0);
      p.setAcks(0);
      return p;
    });
    profile.setName(request.name());
    profile.setEmail(request.email());
    profile.setImageUrl(request.imageUrl());
    Profile saved = profileRepository.save(profile);
    return profileMapper.toDto(saved);
  }
}
