package com.pravesh.user.dto.response;

public record FlatConflictResponse(
        Long occupantResidentId,
        String occupantName,
        String flatNumber
) {}