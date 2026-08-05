package com.pravesh.user.messaging;

public record OtpEventPayload(
        String correlationId,
        Long userId,
        String email,
        String phone,
        String otp,
        String channel
) {}