package com.pravesh.notification.dto.request;

public record ResidentApprovedRequest(
        Long residentId,
        String flatNumber
) {}