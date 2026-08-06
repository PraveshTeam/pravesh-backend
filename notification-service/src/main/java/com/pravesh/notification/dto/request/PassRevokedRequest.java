package com.pravesh.notification.dto.request;

public record PassRevokedRequest(
        Long residentId,
        String visitorName,
        String passUuid
) {}