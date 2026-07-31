package com.pravesh.validation.feign;

public record PassValidationResponse(
        boolean granted,
        String reason,
        Long passId,
        Long residentId,
        String visitorName,
        String passType
) {}