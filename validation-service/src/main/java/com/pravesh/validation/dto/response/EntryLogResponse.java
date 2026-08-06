package com.pravesh.validation.dto.response;

import java.time.LocalDateTime;

public record EntryLogResponse(
        Long id,
        String visitorName,
        Long residentId,
<<<<<<< Updated upstream
=======
        String entryType,
>>>>>>> Stashed changes
        String scanResult,
        String denyReason,
        LocalDateTime scannedAt
) {}