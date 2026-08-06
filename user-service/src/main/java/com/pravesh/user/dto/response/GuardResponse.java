package com.pravesh.user.dto.response;

public record GuardResponse(
        Long userId,
        String name,
        String phone,
        Long gateId,
        String gateName,
        String employeeCode,
        boolean active
) {}