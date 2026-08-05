package com.pravesh.forum.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank(message = "Comment cannot be empty")
        String body
) {}
