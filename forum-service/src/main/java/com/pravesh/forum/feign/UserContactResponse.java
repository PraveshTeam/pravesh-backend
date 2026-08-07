package com.pravesh.forum.feign;

public record UserContactResponse(
        Long id,
        String name,
        String email,
        String phone
) {}
