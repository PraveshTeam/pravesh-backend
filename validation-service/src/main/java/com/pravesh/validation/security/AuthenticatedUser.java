package com.pravesh.validation.security;

public record AuthenticatedUser(Long userId, String email, String role, Long societyId) {}