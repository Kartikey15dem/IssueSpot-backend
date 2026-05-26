package com.example.issuespot.domain.dtos;
import java.time.Instant;
public record ActiveIssuesDto(String level, int totalActiveIssues, Instant updatedAt) {}
