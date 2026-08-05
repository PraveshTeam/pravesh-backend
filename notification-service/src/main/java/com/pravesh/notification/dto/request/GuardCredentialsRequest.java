package com.pravesh.notification.dto.request;

public record GuardCredentialsRequest(
        String phone,
        String tempPassword,
        String gateName
) {}