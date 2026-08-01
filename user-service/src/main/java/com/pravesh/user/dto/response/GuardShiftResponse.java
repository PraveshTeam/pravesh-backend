package com.pravesh.user.dto.response;

import java.time.LocalDateTime;

public record GuardShiftResponse(
        Long shiftId,
        String onDutyName,
        String onDutyEmployeeId,
        LocalDateTime shiftStart,
        LocalDateTime shiftEnd
) {}