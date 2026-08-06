package com.pravesh.notification.dto.response;

public record WebSocketNotificationPayload(
        String type,
        String visitorName,
        String enteredAt,
        String gateName
) {}