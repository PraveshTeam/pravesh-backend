package com.pravesh.validation.feign;

public record VisitorEnteredRequest(
        Long residentId,
        String visitorName,
        String gateName,
        String scannedAt
) {}