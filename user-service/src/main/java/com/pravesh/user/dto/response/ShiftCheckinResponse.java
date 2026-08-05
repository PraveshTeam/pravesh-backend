package com.pravesh.user.dto.response;

import java.time.LocalDateTime;

public record ShiftCheckinResponse(
        Long shiftId,
        Long gateId,
        String onDutyName,
        LocalDateTime shiftStart
) {}