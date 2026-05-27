package com.example.issuespot.services.impl;
import com.example.issuespot.domain.dtos.*;
import com.example.issuespot.domain.entities.*;
import com.example.issuespot.domain.enums.*;
import com.example.issuespot.exceptions.*;
import com.example.issuespot.mappers.PostMapper;
import com.example.issuespot.repositories.*;
import com.example.issuespot.services.PostService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final ProfileRepository profileRepository;
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
    return toPagedResponse(postsPage);
  }

  @Override @Transactional(readOnly = true)
  public ActiveIssuesDto getActiveIssuesCount(String level) {
    return activeIssuesCountRepository.findById(level.toUpperCase()).map(c -> new ActiveIssuesDto(c.getLevel(), c.getTotalActiveIssues(), c.getUpdatedAt()))
        .orElse(new ActiveIssuesDto(level.toUpperCase(), 0, Instant.now()));
  }

  @Override @Transactional
  public PostWithProfileDto createPost(UUID userId, CreatePostRequest request) {
    Profile profile = profileRepository.findById(userId).orElseThrow(() -> new BadRequestException("Profile missing"));
    Post post = new Post();
    post.setUserId(userId);
    post.setPostLevel(PostLevel.valueOf(request.postLevel().toUpperCase()));
    post.setPostText(request.postText());
    try { post.setMediaType(MediaType.valueOf(request.mediaType().toUpperCase())); } catch(Exception e) { post.setMediaType(MediaType.TEXT); }
    post.setMediaUrl(request.mediaUrl());
    post.setLocality(request.locality());
    post.setDistrict(request.district());
    post.setState(request.state());
    post.setCountry(request.country());
    if (request.coordinates() != null) {
        post.setCoordinates(geometryFactory.createPoint(new Coordinate(request.coordinates().longitude(), request.coordinates().latitude())));
    }
    return postMapper.toDto(postRepository.save(post), profile);
  }

  @Override @Transactional
  public void deletePost(UUID userId, UUID postId) {
    Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));
    if (!post.getUserId().equals(userId)) throw new BadRequestException("Not allowed");
    postRepository.delete(post);
  }

  @Override @Transactional
  public void toggleLike(UUID userId, UUID postId) {
    PostAckId id = new PostAckId(userId, postId);
    if (postAckRepository.existsById(id)) postAckRepository.deleteById(id);
    else {
        PostAck ack = new PostAck(); ack.setId(id);
        postAckRepository.save(ack);
    }
  }

  @Override @Transactional(readOnly = true)
  public PagedResponse<CommentDto> getComments(UUID postId, int page, int limit) {
    Page<PostComment> commentsPage = postCommentRepository.findByPostIdOrderByCreatedAtDesc(postId, PageRequest.of(page, limit));
    Set<UUID> userIds = new HashSet<>();
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
    
    return new PagedResponse<>(dtos, prevKey, nextKey);
  }

  @Override @Transactional
  public void addComment(UUID userId, UUID postId, String comment) {
    PostComment c = new PostComment();
    c.setPostId(postId); c.setUserId(userId); c.setCommentText(comment);
    postCommentRepository.save(c);
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
  public PagedResponse<PostWithProfileDto> getPostsByUser(UUID userId, int page, int limit) {
    return toPagedResponse(postRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, limit)));
  }

  @Override @Transactional(readOnly = true)
  public PagedResponse<PostWithProfileDto> getPostsLikedByUser(UUID userId, int page, int limit) {
    return toPagedResponse(postRepository.findLikedPostsByUser(userId, PageRequest.of(page, limit)));
  }

  private PagedResponse<PostWithProfileDto> toPagedResponse(Page<Post> postsPage) {
    Set<UUID> userIds = new HashSet<>();
    postsPage.getContent().forEach(p -> userIds.add(p.getUserId()));
    Map<UUID, Profile> profileMap = new HashMap<>();
    profileRepository.findAllById(userIds).forEach(pr -> profileMap.put(pr.getId(), pr));
    List<PostWithProfileDto> dtos = postsPage.getContent().stream().map(p -> postMapper.toDto(p, profileMap.get(p.getUserId()))).toList();
    return new PagedResponse<>(dtos, postsPage.hasPrevious() ? postsPage.getNumber() - 1 : null, postsPage.hasNext() ? postsPage.getNumber() + 1 : null);
  }
}
