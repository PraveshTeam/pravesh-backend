package com.pravesh.notification.messaging;

public record OtpEventPayload(
        String correlationId,
        Long userId,
        String email,
        String phone,
        String otp,
<<<<<<< Updated upstream
        String channel
=======
        String channel,
        String purpose // "PASSWORD_RESET" or "REGISTRATION_VERIFICATION" 
>>>>>>> Stashed changes
) {}