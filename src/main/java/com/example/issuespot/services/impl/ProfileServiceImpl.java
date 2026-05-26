package com.example.issuespot.services.impl;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.domain.entities.Profile;
import com.example.issuespot.exceptions.NotFoundException;
import com.example.issuespot.mappers.ProfileMapper;
import com.example.issuespot.repositories.ProfileRepository;
import com.example.issuespot.services.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
  private final ProfileRepository profileRepository;
  private final ProfileMapper profileMapper;

  @Override @Transactional(readOnly = true)
  public ProfileDto getProfile(UUID userId) {
    return profileRepository.findById(userId).map(profileMapper::toDto)
        .orElseThrow(() -> new NotFoundException("Profile not found"));
  }

  @Override @Transactional
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
    return profileMapper.toDto(profileRepository.save(profile));
  }
}
