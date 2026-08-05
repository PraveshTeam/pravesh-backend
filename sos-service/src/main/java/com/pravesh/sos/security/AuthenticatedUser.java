package com.pravesh.sos.security;

public record AuthenticatedUser(
        Long userId,
        String email,
        String role,
        Long societyId
) {}