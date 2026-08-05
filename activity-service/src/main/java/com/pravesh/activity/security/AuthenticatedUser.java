package com.pravesh.activity.security;

public record AuthenticatedUser(
        Long userId,
        String email,
        String role,
        Long societyId
) {}
