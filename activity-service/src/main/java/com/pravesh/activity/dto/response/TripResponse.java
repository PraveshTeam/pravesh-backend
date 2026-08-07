package com.pravesh.activity.dto.response;

import com.pravesh.activity.entity.TripStatus;
import java.time.LocalDateTime;

public record TripResponse(
        Long id,
        Long creatorId,
        String creatorName,
        String title,
        String description,
        int capacity,
        int acceptedCount,
        TripStatus status,
<<<<<<< Updated upstream
        LocalDateTime createdAt
) {}
=======
        LocalDateTime createdAt,
        String myRequestStatus 
) {}
>>>>>>> Stashed changes
