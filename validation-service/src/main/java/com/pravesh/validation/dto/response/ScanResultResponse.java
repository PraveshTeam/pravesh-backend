package com.pravesh.validation.dto.response;

public record ScanResultResponse(
        boolean granted,
        String reason,
        String visitorName,
        String passType
) {}