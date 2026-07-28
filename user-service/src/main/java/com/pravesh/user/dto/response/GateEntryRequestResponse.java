package com.pravesh.user.dto.response;

import com.pravesh.user.entity.GateRequestStatus;
import java.time.LocalDateTime;

public record GateEntryRequestResponse(
        Long id,
        String visitorName,
        String visitorPhone,
        String claimedFlatNumber,
        String reason,
        GateRequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {}