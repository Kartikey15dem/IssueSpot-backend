package com.example.issuespot.services;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.domain.entities.*;
import com.example.issuespot.exceptions.BadRequestException;
import com.example.issuespot.exceptions.PostNotFoundException;
import com.example.issuespot.mappers.PostMapper;
import com.example.issuespot.repositories.*;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
  private static final Set<String> VALID_POST_LEVELS = Set.of("LOCALITY", "DISTRICT", "STATE", "NATIONAL");
  private static final Set<String> VALID_MEDIA_TYPES = Set.of("IMAGE", "VIDEO", "GIF", "PDF");
  private final PostRepository postRepository;
  private final ProfileRepository profileRepository;
  private final PostAckRepository postAckRepository;
  private final PostCommentRepository postCommentRepository;
  private final PostReportRepository postReportRepository;
  private final PostShareRepository postShareRepository;
  private final ActiveIssuesCountRepository activeIssuesCountRepository;
  private final PostMapper postMapper;
  private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

  @Transactional(readOnly = true)
  public PagedResponse<PostWithProfileDto> getPostsByLevel(
      String level, String locality, String district, String state, String country, Double lat, Double lon, int page, int limit) {
    String normalized = normalizePostLevel(level);
    Pageable pageable = PageRequest.of(page, limit);
    Page<Post> postsPage;
    
    if (normalized.equals("LOCALITY")) {
        if (lat != null && lon != null) {
            postsPage = postRepository.findByPostLevelAndCoordinatesNear(normalized, lat, lon, 10000.0, pageable);
        } else if (locality != null) {
            postsPage = postRepository.findByPostLevelAndLocalityOrderByCreatedAtDesc(normalized, locality, pageable);
        } else {
            postsPage = postRepository.findByPostLevelOrderByCreatedAtDesc(normalized, pageable);
        }
    } else if (district != null && normalized.equals("DISTRICT")) {
        postsPage = postRepository.findByPostLevelAndDistrictOrderByCreatedAtDesc(normalized, district, pageable);
    } else if (state != null && normalized.equals("STATE")) {
        postsPage = postRepository.findByPostLevelAndStateOrderByCreatedAtDesc(normalized, state, pageable);
    } else if (country != null && normalized.equals("NATIONAL")) {
        postsPage = postRepository.findByPostLevelAndCountryOrderByCreatedAtDesc(normalized, country, pageable);
    } else {
        postsPage = postRepository.findByPostLevelOrderByCreatedAtDesc(normalized, pageable);
    }
    return toPagedResponse(postsPage);
  }

  @Transactional(readOnly = true)
  public ActiveIssuesDto getActiveIssuesCount(String level) {
    String normalized = normalizePostLevel(level);
    ActiveIssuesCount count = activeIssuesCountRepository.findById(normalized).orElseGet(() -> {
          ActiveIssuesCount empty = new ActiveIssuesCount();
          empty.setLevel(normalized);
          empty.setTotalActiveIssues(0);
          empty.setUpdatedAt(Instant.now());
          return empty;
        });
    return new ActiveIssuesDto(count.getLevel(), count.getTotalActiveIssues(), count.getUpdatedAt());
  }

  @Transactional
  public PostWithProfileDto createPost(UUID userId, CreatePostRequest request) {
    Profile profile = profileRepository.findById(userId).orElseThrow(() -> new BadRequestException("Profile missing for user"));
    Post post = new Post();
    post.setUserId(userId);
    post.setPostLevel(normalizePostLevel(request.postLevel()));
    post.setPostText(request.postText());
    post.setMediaType(normalizeMediaType(request.mediaType()));
    post.setMediaUrl(request.mediaUrl());
    post.setLocality(request.locality());
    post.setDistrict(request.district());
    post.setState(request.state());
    post.setCountry(request.country());
    post.setCoordinates(toPoint(request.coordinates()));
    post.setLikes(0);
    post.setComments(0);
    Post saved = postRepository.save(post);
    return postMapper.toDto(saved, profile);
  }

  @Transactional
  public void deletePost(UUID userId, UUID postId) {
    Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));
    if (!post.getUserId().equals(userId)) throw new BadRequestException("Not allowed");
    postRepository.delete(post);
  }

  @Transactional
  public void toggleLike(UUID userId, UUID postId) {
    if (!postRepository.existsById(postId)) throw new PostNotFoundException("Post not found");
    PostAckId id = new PostAckId(userId, postId);
    if (postAckRepository.existsById(id)) {
      postAckRepository.deleteById(id);
      return;
    }
    PostAck ack = new PostAck();
    ack.setId(id);
    postAckRepository.save(ack);
  }

  @Transactional
  public void addComment(UUID userId, UUID postId, String comment) {
    if (!postRepository.existsById(postId)) throw new PostNotFoundException("Post not found");
    PostComment c = new PostComment();
    c.setPostId(postId);
    c.setUserId(userId);
    c.setCommentText(comment);
    postCommentRepository.save(c);
  }

  @Transactional
  public void reportPost(UUID userId, UUID postId, String reason) {
    if (!postRepository.existsById(postId)) throw new PostNotFoundException("Post not found");
    PostReport report = new PostReport();
    report.setPostId(postId);
    report.setUserId(userId);
    report.setReason(reason);
    postReportRepository.save(report);
  }

  @Transactional
  public void sharePost(UUID userId, UUID postId) {
    if (!postRepository.existsById(postId)) throw new PostNotFoundException("Post not found");
    PostShare share = new PostShare();
    share.setPostId(postId);
    share.setUserId(userId);
    postShareRepository.save(share);
  }

  @Transactional(readOnly = true)
  public PagedResponse<PostWithProfileDto> getPostsByUser(UUID userId, int page, int limit) {
    Pageable pageable = PageRequest.of(page, limit);
    return toPagedResponse(postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable));
  }

  @Transactional(readOnly = true)
  public PagedResponse<PostWithProfileDto> getPostsLikedByUser(UUID userId, int page, int limit) {
    Pageable pageable = PageRequest.of(page, limit);
    return toPagedResponse(postRepository.findLikedPostsByUser(userId, pageable));
  }

  private PagedResponse<PostWithProfileDto> toPagedResponse(Page<Post> postsPage) {
    List<PostWithProfileDto> items = mapPostsWithProfiles(postsPage.getContent());
    Integer prevKey = postsPage.hasPrevious() ? postsPage.getNumber() - 1 : null;
    Integer nextKey = postsPage.hasNext() ? postsPage.getNumber() + 1 : null;
    return new PagedResponse<>(items, prevKey, nextKey);
  }

  private List<PostWithProfileDto> mapPostsWithProfiles(List<Post> posts) {
    Set<UUID> userIds = new HashSet<>();
    for (Post p : posts) userIds.add(p.getUserId());
    Map<UUID, Profile> profileMap = new HashMap<>();
    for (Profile profile : profileRepository.findAllById(userIds)) profileMap.put(profile.getId(), profile);
    return posts.stream().map(p -> postMapper.toDto(p, profileMap.get(p.getUserId()))).toList();
  }

  private Point toPoint(CoordinatesDto dto) {
    if (dto == null) return null;
    return geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude()));
  }

  private String normalizePostLevel(String raw) {
    if (raw == null) throw new BadRequestException("postLevel required");
    String normalized = raw.trim().toUpperCase();
    if (!VALID_POST_LEVELS.contains(normalized)) throw new BadRequestException("Invalid postLevel");
    return normalized;
  }

  private String normalizeMediaType(String raw) {
    if (raw == null) throw new BadRequestException("mediaType required");
    String normalized = raw.trim().toUpperCase();
    if (!VALID_MEDIA_TYPES.contains(normalized)) throw new BadRequestException("Invalid mediaType");
    return normalized;
  }
}
