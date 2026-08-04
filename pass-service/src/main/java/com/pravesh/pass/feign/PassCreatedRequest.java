package com.pravesh.pass.feign;

public record PassCreatedRequest(
        Long residentId,
        String flatNumber,
        String visitorName,
        String passUuid,
        String validFrom,
        String validUntil,
        String qrBase64
) {}