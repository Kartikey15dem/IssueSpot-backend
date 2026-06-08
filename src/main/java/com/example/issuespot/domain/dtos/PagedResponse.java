package com.example.issuespot.domain.dtos;
import java.util.List;
public record PagedResponse<T>(List<T> items, Integer prevKey, Integer nextKey, Integer activeIssuesCount) {}
