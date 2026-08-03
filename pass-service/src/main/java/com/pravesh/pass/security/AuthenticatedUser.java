package com.pravesh.pass.security;

public record AuthenticatedUser(Long userId, String email, String role, Long societyId) {}