package com.pravesh.sos.dto.response;

import com.pravesh.sos.entity.SosCategory;
import com.pravesh.sos.entity.SosStatus;
import java.time.LocalDateTime;

public record SosAlertResponse(
        Long id,
        String residentName,
        String flatNumber,
        String phone,
        SosCategory category,
        String description,
        SosStatus status,
        LocalDateTime createdAt,
        LocalDateTime acknowledgedAt,
        LocalDateTime resolvedAt
) {}