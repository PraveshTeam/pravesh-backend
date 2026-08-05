package com.pravesh.notification.security;

public record AuthenticatedUser(Long userId, String email, String role, Long societyId) {}