package com.pravesh.user.feign;

public record GateEntryNotifyRequest(
        Long residentUserId,
        String residentPhone,
        String visitorName,
        String flatNumber,
        Long requestId
) {}