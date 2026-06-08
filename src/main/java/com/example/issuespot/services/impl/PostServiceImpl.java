package com.example.issuespot.services.impl;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.domain.entities.*;
import com.example.issuespot.domain.enums.*;
import com.example.issuespot.exceptions.*;
import com.example.issuespot.mappers.PostMapper;
import com.example.issuespot.repositories.*;
import com.example.issuespot.services.PostService;
import com.example.issuespot.services.S3StorageService;
import com.example.issuespot.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final ProfileRepository profileRepository;
  private final S3StorageService s3StorageService;
  private final PostAckRepository postAckRepository;
  private final PostCommentRepository postCommentRepository;
  private final PostReportRepository postReportRepository;
  private final PostShareRepository postShareRepository;
  private final ActiveIssuesCountRepository activeIssuesCountRepository;
  private final PostMapper postMapper;
  private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

  @Override @Transactional(readOnly = true)
  public PagedResponse<PostWithProfileDto> getPostsByLevel(String level, String locality, String district, String state, String country, Double lat, Double lon, int page, int limit) {
    PostLevel pl = PostLevel.valueOf(level.toUpperCase());
    Pageable pageable = PageRequest.of(page, limit);
    Page<Post> postsPage;
    
    if (pl == PostLevel.LOCALITY) {
        if (lat != null && lon != null) {
            postsPage = postRepository.findByPostLevelAndCoordinatesNear(pl, lat, lon, 10000.0, pageable);
        } else if (locality != null) {
            postsPage = postRepository.findByPostLevelAndLocalityOrderByCreatedAtDesc(pl, locality, pageable);
        } else {
            postsPage = postRepository.findByPostLevelOrderByCreatedAtDesc(pl, pageable);
        }
    } else if (pl == PostLevel.DISTRICT && district != null) {
        postsPage = postRepository.findByPostLevelAndDistrictOrderByCreatedAtDesc(pl, district, pageable);
    } else if (pl == PostLevel.STATE && state != null) {
        postsPage = postRepository.findByPostLevelAndStateOrderByCreatedAtDesc(pl, state, pageable);
    } else if (pl == PostLevel.NATIONAL && country != null) {
        postsPage = postRepository.findByPostLevelAndCountryOrderByCreatedAtDesc(pl, country, pageable);
    } else {
        postsPage = postRepository.findByPostLevelOrderByCreatedAtDesc(pl, pageable);
    }
    int activeIssuesCount = activeIssuesCountRepository.findById(pl.name()).map(c -> c.getTotalActiveIssues()).orElse(0);
    return toPagedResponse(postsPage, activeIssuesCount);
  }

  @Override @Transactional(readOnly = true)
  public ActiveIssuesDto getActiveIssuesCount(String level) {
    return activeIssuesCountRepository.findById(level.toUpperCase()).map(c -> new ActiveIssuesDto(c.getLevel(), c.getTotalActiveIssues(), c.getUpdatedAt()))
        .orElse(new ActiveIssuesDto(level.toUpperCase(), 0, Instant.now()));
  }

  @Override @Transactional
  public PostWithProfileDto createPost(UUID userId, CreatePostRequest request, List<MultipartFile> files) {
    Profile profile = profileRepository.findById(userId).orElseThrow(() -> new BadRequestException("Profile missing"));
    Post post = new Post();
    post.setUserId(userId);
    post.setPostLevel(PostLevel.LOCALITY);
    post.setPostText(request.postText());
    try { post.setMediaType(MediaType.valueOf(request.mediaType().toUpperCase())); } catch(Exception e) { post.setMediaType(MediaType.TEXT); }
        List<String> finalMediaUrls = new ArrayList<>();
    if (request.mediaUrls() != null) {
        finalMediaUrls.addAll(request.mediaUrls()); // Keep any already resolved URLs
    }
    if (files != null && !files.isEmpty()) {
        finalMediaUrls.addAll(s3StorageService.uploadFiles(files));
    }
    post.setMediaUrls(finalMediaUrls.isEmpty() ? null : finalMediaUrls);
    post.setLocality(request.locality());
    post.setDistrict(request.district());
    post.setState(request.state());
    post.setCountry(request.country());
    if (request.coordinates() != null) {
        post.setCoordinates(geometryFactory.createPoint(new Coordinate(request.coordinates().longitude(), request.coordinates().latitude())));
    }
        Post savedPost = postRepository.save(post);
        
        // Increment profile post count
        profile.setTotalPosts(profile.getTotalPosts() + 1);
        profileRepository.save(profile);

        // Update active issues count
        activeIssuesCountRepository.findById(savedPost.getPostLevel().name()).ifPresentOrElse(
            c -> {
                c.setTotalActiveIssues(c.getTotalActiveIssues() + 1);
                activeIssuesCountRepository.save(c);
            },
            () -> {
                ActiveIssuesCount c = new ActiveIssuesCount();
                c.setLevel(savedPost.getPostLevel().name());
                c.setTotalActiveIssues(1);
                c.setUpdatedAt(Instant.now());
                activeIssuesCountRepository.save(c);
            }
        );
    
    return postMapper.toDto(savedPost, profile, false, false);
  }

  @Override @Transactional
  public void deletePost(UUID userId, UUID postId) {
    Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));
    if (!post.getUserId().equals(userId)) throw new BadRequestException("Not allowed");
    postRepository.delete(post);
    // Update active issues count
    activeIssuesCountRepository.findById(post.getPostLevel().name()).ifPresent(c -> {
        c.setTotalActiveIssues(Math.max(0, c.getTotalActiveIssues() - 1));
        activeIssuesCountRepository.save(c);
    });
  }

  @Override @Transactional
  public void toggleLike(UUID userId, UUID postId) {
    PostAckId id = new PostAckId(userId, postId);
    Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));
    if (postAckRepository.existsById(id)) {
        postAckRepository.deleteById(id);
        post.setLikes(Math.max(0, post.getLikes() - 1));
    } else {
        PostAck ack = new PostAck(); ack.setId(id);
        postAckRepository.save(ack);
        post.setLikes(post.getLikes() + 1);
    }
    postRepository.save(post);
  }

  @Override @Transactional(readOnly = true)
  public PagedResponse<CommentDto> getComments(UUID postId, int page, int limit) {
    Page<PostComment> commentsPage = postCommentRepository.findByPostIdOrderByCreatedAtDesc(postId, PageRequest.of(page, limit));
    Optional<UUID> currentUserId = SecurityUtil.currentUserId(); Set<UUID> userIds = new HashSet<>();
    for (PostComment c : commentsPage.getContent()) {
        userIds.add(c.getUserId());
    }
    Map<UUID, Profile> profileMap = new HashMap<>();
    for (Profile profile : profileRepository.findAllById(userIds)) {
        profileMap.put(profile.getId(), profile);
    }
    
    List<CommentDto> dtos = commentsPage.getContent().stream().map(c -> {
        Profile p = profileMap.get(c.getUserId());
        ProfileInfoDto profileInfo = p != null ? new ProfileInfoDto(p.getId(), p.getName(), p.getImageUrl()) : null;
        return new CommentDto(c.getId(), c.getPostId(), c.getCommentText(), c.getCreatedAt(), profileInfo);
    }).toList();
    
    Integer prevKey = commentsPage.hasPrevious() ? commentsPage.getNumber() - 1 : null;
    Integer nextKey = commentsPage.hasNext() ? commentsPage.getNumber() + 1 : null;
    
    return new PagedResponse<>(dtos, prevKey, nextKey, null);
  }

  @Override @Transactional
  public void addComment(UUID userId, UUID postId, String comment) {
    Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));
    PostComment c = new PostComment();
    c.setPostId(postId); c.setUserId(userId); c.setCommentText(comment);
    postCommentRepository.save(c);
    post.setComments(post.getComments() + 1);
    postRepository.save(post);
  }

  @Override @Transactional
  public void reportPost(UUID userId, UUID postId, String reason) {
    PostReport r = new PostReport();
    r.setPostId(postId); r.setUserId(userId); r.setReason(reason);
    postReportRepository.save(r);
  }

  @Override @Transactional
  public void sharePost(UUID userId, UUID postId) {
    PostShare s = new PostShare();
    s.setPostId(postId); s.setUserId(userId);
    postShareRepository.save(s);
  }

  @Override @Transactional(readOnly = true)
  public PagedResponse<PostWithProfileDto> getPostsByUser(UUID userId, String sort, int page, int limit) {
    Pageable pageable = PageRequest.of(page, limit);
    Page<Post> postsPage = switch (sort.toUpperCase()) {
        case "OLDEST" -> postRepository.findByUserIdOrderByCreatedAtAsc(userId, pageable);
        case "POPULAR" -> postRepository.findByUserIdOrderByLikesDesc(userId, pageable);
        default -> postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    };
    return toPagedResponse(postsPage, null);
  }

  @Override @Transactional(readOnly = true)
  public PagedResponse<PostWithProfileDto> getPostsLikedByUser(UUID userId, String sort, int page, int limit) {
    Pageable pageable = PageRequest.of(page, limit);
    Page<Post> postsPage = switch (sort.toUpperCase()) {
        case "OLDEST" -> postRepository.findLikedPostsByUserOrderByCreatedAtAsc(userId, pageable);
        case "POPULAR" -> postRepository.findLikedPostsByUserOrderByLikesDesc(userId, pageable);
        default -> postRepository.findLikedPostsByUserOrderByCreatedAtDesc(userId, pageable);
    };
    return toPagedResponse(postsPage, null);
  }

  private PagedResponse<PostWithProfileDto> toPagedResponse(Page<Post> postsPage, Integer activeIssuesCount) {
    Optional<UUID> currentUserId = SecurityUtil.currentUserId(); Set<UUID> userIds = new HashSet<>();
    postsPage.getContent().forEach(p -> userIds.add(p.getUserId()));
    Map<UUID, Profile> profileMap = new HashMap<>();
    profileRepository.findAllById(userIds).forEach(pr -> profileMap.put(pr.getId(), pr));
    List<PostWithProfileDto> dtos = postsPage.getContent().stream().map(p -> { boolean isLiked = currentUserId.map(uid -> postAckRepository.existsById(new PostAckId(uid, p.getId()))).orElse(false); boolean isReported = currentUserId.map(uid -> postReportRepository.existsByPostIdAndUserId(p.getId(), uid)).orElse(false); return postMapper.toDto(p, profileMap.get(p.getUserId()), isLiked, isReported); }).toList();
    return new PagedResponse<>(dtos, postsPage.hasPrevious() ? postsPage.getNumber() - 1 : null, postsPage.hasNext() ? postsPage.getNumber() + 1 : null, null);
  }

  @Override @Transactional(readOnly = true)
  public PagedResponse<PostWithProfileDto> searchPosts(String query, String level, int page, int limit) {
    Page<Post> postsPage = postRepository.findByPostTextContainingIgnoreCaseAndPostLevel(query, PostLevel.valueOf(level.toUpperCase()), PageRequest.of(page, limit));
    List<PostWithProfileDto> dtos = new ArrayList<>();
    Map<UUID, Profile> profileMap = new HashMap<>();
    Optional<UUID> currentUserId = SecurityUtil.currentUserId(); Set<UUID> userIds = new HashSet<>();
    for (Post p : postsPage.getContent()) userIds.add(p.getUserId());
    for (Profile prof : profileRepository.findAllById(userIds)) profileMap.put(prof.getId(), prof);
    for (Post p : postsPage.getContent()) {
        Profile prof = profileMap.get(p.getUserId());
        boolean isLiked = currentUserId.map(uid -> postAckRepository.existsById(new PostAckId(uid, p.getId()))).orElse(false); boolean isReported = currentUserId.map(uid -> postReportRepository.existsByPostIdAndUserId(p.getId(), uid)).orElse(false); dtos.add(postMapper.toDto(p, prof, isLiked, isReported));
    }
    return new PagedResponse<>(dtos, postsPage.hasPrevious() ? postsPage.getNumber() - 1 : null, postsPage.hasNext() ? postsPage.getNumber() + 1 : null, null);
  }

  @Override @Transactional(readOnly = true)
  public PostWithProfileDto getPostById(UUID postId) {
    Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));
    Profile profile = profileRepository.findById(post.getUserId()).orElse(null);
    Optional<UUID> currentUserId2 = SecurityUtil.currentUserId(); boolean isLiked = currentUserId2.map(uid -> postAckRepository.existsById(new PostAckId(uid, post.getId()))).orElse(false); boolean isReported = currentUserId2.map(uid -> postReportRepository.existsByPostIdAndUserId(post.getId(), uid)).orElse(false); return postMapper.toDto(post, profile, isLiked, isReported);
  }
}