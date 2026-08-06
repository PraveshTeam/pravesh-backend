package com.pravesh.notification.feign;

public record UserContactResponse(
        Long id,
        String name,
        String email,
        String phone
) {}