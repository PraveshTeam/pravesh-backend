package com.pravesh.analytics.security;

public record AuthenticatedUser(Long userId, String email, String role, Long societyId) {}