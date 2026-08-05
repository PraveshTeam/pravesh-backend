package com.pravesh.activity.feign;

// Confirmed UNWRAPPED via direct Postman test in payment-service (same
// endpoint) -- this one returns the plain object, not {success,message,data}.
public record ResidentContextResponse(
        Long userId,
        String name,
        String phone,
        Long flatId,
        String flatNumber,
        Long societyId
) {}
