package com.pravesh.payment.feign;

public record ResidentContextResponse(
        Long userId,
        String name,
        String phone,
        Long flatId,
        String flatNumber,
        Long societyId
) {}
