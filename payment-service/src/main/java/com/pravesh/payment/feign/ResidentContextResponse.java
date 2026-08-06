package com.pravesh.payment.feign;

// Mirrors sos-service's ResidentContextResponse -- same internal endpoint,
// same shape, since user-service serves one canonical context payload.
public record ResidentContextResponse(
        Long userId,
        String name,
        String phone,
        Long flatId,
        String flatNumber,
        Long societyId
) {}
