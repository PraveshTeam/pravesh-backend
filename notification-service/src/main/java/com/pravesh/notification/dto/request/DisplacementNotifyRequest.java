package com.pravesh.notification.dto.request;

public record DisplacementNotifyRequest(
        Long residentUserId,
        String residentPhone,
        String oldFlatNumber
) {}