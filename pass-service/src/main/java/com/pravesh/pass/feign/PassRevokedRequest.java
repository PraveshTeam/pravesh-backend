package com.pravesh.pass.feign;

public record PassRevokedRequest(
        Long residentId,
        String visitorName,
        String passUuid
) {}