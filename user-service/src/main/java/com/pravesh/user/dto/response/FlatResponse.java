package com.pravesh.user.dto.response;

public record FlatResponse(
        Long id,
        String flatNumber,
        String tower,
        Long residentId
) {}