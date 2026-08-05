package com.pravesh.user.dto.response;

public record ShiftStatusResponse(
        boolean hasActiveShift,
        Long shiftId
) {}