package com.pravesh.sos.feign;

public record UserContactResponse(
        Long id,
        String name,
        String email,
        String phone
) {}
