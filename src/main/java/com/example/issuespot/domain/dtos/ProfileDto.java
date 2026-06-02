package com.example.issuespot.domain.dtos;
import java.util.List;
import java.util.UUID;
public record ProfileDto(UUID id, String name, String email, String imageUrl, int totalPosts, int acks, List<Integer> postByArea) {}
