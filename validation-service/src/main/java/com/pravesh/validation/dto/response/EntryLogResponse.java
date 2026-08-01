package com.pravesh.validation.dto.response;

import java.time.LocalDateTime;

public record EntryLogResponse(
        Long id,
        String visitorName,
        Long residentId,
        String scanResult,
        String denyReason,
        LocalDateTime scannedAt
) {}