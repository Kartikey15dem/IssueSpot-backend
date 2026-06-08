package com.example.issuespot.services.impl;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.domain.entities.Profile;
import com.example.issuespot.domain.entities.AppUser;
import com.example.issuespot.exceptions.NotFoundException;
import com.example.issuespot.exceptions.BadRequestException;
import com.example.issuespot.mappers.ProfileMapper;
import com.example.issuespot.repositories.PostRepository;
import com.example.issuespot.repositories.AppUserRepository;
import com.example.issuespot.domain.enums.PostLevel;
import java.util.List;
import java.util.Arrays;
import com.example.issuespot.repositories.ProfileRepository;
import com.example.issuespot.services.ProfileService;
import com.example.issuespot.services.AuthService;
import com.example.issuespot.services.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
  private final ProfileRepository profileRepository;
  private final ProfileMapper profileMapper;
  private final PostRepository postRepository;
  private final S3StorageService s3StorageService;
  private final AuthService authService;
  private final AppUserRepository appUserRepository;

  @Override @Transactional(readOnly = true)
  public ProfileDto getProfile(UUID userId) {
    Profile profile = profileRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("Profile not found", "Profile not found."));
    
    return profileMapper.toDto(profile, calculatePostByArea(userId));
  }
  
  private List<Integer> calculatePostByArea(UUID userId) {
      List<Object[]> results = postRepository.countUserPostsGroupedByLevel(userId);
      // Array mapping: [LOCALITY, DISTRICT, STATE, NATIONAL]
      int[] counts = new int[4];
      for (Object[] row : results) {
          PostLevel level = (PostLevel) row[0];
          long count = ((Number) row[1]).longValue();
          switch (level) {
              case LOCALITY -> counts[0] = (int) count;
              case DISTRICT -> counts[1] = (int) count;
              case STATE -> counts[2] = (int) count;
              case NATIONAL -> counts[3] = (int) count;
          }
      }
      return Arrays.asList(counts[0], counts[1], counts[2], counts[3]);
  }

  @Override @Transactional
  public ProfileDto upsertProfile(UUID userId, UpsertProfileRequest request, MultipartFile file) {
    Profile profile = profileRepository.findById(userId).orElseGet(() -> {
      Profile p = new Profile();
      p.setId(userId);
      p.setTotalPosts(0);
      p.setAcks(0);
      return p;
    });
    profile.setName(request.name());
    profile.setEmail(request.email());
    if (file != null && !file.isEmpty()) {
        profile.setImageUrl(s3StorageService.uploadFiles(List.of(file)).get(0));
    } else {
        profile.setImageUrl(request.imageUrl());
    }
    return profileMapper.toDto(profileRepository.save(profile), calculatePostByArea(userId));
  }

  @Override @Transactional
  public void requestEmailChange(UUID userId, String newEmail) {
    if (appUserRepository.findByEmail(newEmail).isPresent()) {
        throw new BadRequestException("Email already in use", "This email is already in use.");
    }
    authService.requestOtp(newEmail);
  }

  @Override @Transactional
  public void verifyEmailChange(UUID userId, String newEmail, String code) {
    authService.verifyOtpOnly(newEmail, code);
    
    AppUser user = appUserRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found", "We couldn't find your account. Please sign in again."));
    user.setEmail(newEmail);
    appUserRepository.save(user);

    Profile profile = profileRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("Profile not found", "Profile not found."));
    profile.setEmail(newEmail);
    profileRepository.save(profile);
  }
}
