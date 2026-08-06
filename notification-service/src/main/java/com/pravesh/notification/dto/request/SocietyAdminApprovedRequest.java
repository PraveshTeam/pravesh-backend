package com.pravesh.notification.dto.request;

public record SocietyAdminApprovedRequest(
        Long adminId,
        String societyName
) {}