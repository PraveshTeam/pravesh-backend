package com.pravesh.notification.dto.response;

import java.time.Instant;
import java.util.List;

public record NotificationResponse(
        String id,
        String type,
        List<String> channel,
        String title,
        String message,
        boolean isRead,
        Instant createdAt
) {}