package com.pravesh.activity.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long authorId,
        String authorName,
        String body,
        LocalDateTime createdAt
) {}
