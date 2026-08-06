package com.pravesh.activity.dto.response;

import com.pravesh.activity.entity.JoinRequestStatus;
import java.time.LocalDateTime;

public record JoinRequestResponse(
        Long id,
        Long tripId,
        Long requesterId,
        String requesterName,
        JoinRequestStatus status,
        LocalDateTime createdAt
) {}
