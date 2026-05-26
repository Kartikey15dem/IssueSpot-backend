package com.example.issuespot.controllers;
import com.example.issuespot.domain.dtos.ActiveIssuesDto;
import com.example.issuespot.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/active_issues_count") @RequiredArgsConstructor
public class ActiveIssuesController {
  private final PostService postService;
  @GetMapping public ActiveIssuesDto getCount(@RequestParam String level) { return postService.getActiveIssuesCount(level); }
}
