package com.pravesh.notification.dto.request;

public record GateEntryNotifyRequest(
        Long residentUserId,
        String residentPhone,
        String visitorName,
        String flatNumber,
        Long requestId
) {}