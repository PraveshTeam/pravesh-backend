package com.pravesh.activity.feign;

public record UserContactResponse(
        Long id,
        String name,
        String email,
        String phone
) {}
