package com.pravesh.user.feign;

public record DisplacementNotifyRequest(
        Long residentUserId,
        String residentPhone,
        String oldFlatNumber
) {}