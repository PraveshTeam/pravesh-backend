package com.pravesh.validation.feign;

public record ShiftStatusResponse(
        boolean hasActiveShift,
        Long shiftId
) {}