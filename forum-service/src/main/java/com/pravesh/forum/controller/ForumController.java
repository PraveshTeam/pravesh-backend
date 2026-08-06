package com.pravesh.forum.controller;

import com.pravesh.forum.dto.request.CreateCommentRequest;
import com.pravesh.forum.dto.request.CreatePostRequest;
import com.pravesh.forum.dto.response.ApiResponse;
import com.pravesh.forum.dto.response.CommentResponse;
import com.pravesh.forum.dto.response.PostResponse;
import com.pravesh.forum.security.AuthenticatedUser;
import com.pravesh.forum.service.ForumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {


    @GetMapping("/posts")
    public ApiResponse<List<PostResponse>> listPosts(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(required = false) String category) {
        return ApiResponse.ok("Posts", forumService.listPosts(category, caller.societyId()));
    }
    
    @PostMapping("/posts")
    public ApiResponse<PostResponse> createPost(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreatePostRequest req) {
        return ApiResponse.ok("Post created", forumService.createPost(req, caller.userId(), caller.societyId()));
    }
    }
