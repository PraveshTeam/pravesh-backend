package com.pravesh.user.feign;

public record GuardCredentialsRequest(
        String phone,
        String tempPassword,
        String gateName
) {}