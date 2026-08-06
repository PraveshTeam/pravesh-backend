package com.pravesh.forum.security;

public record AuthenticatedUser(
        Long userId,
        String email,
        String role,
        Long societyId
) {}
