package com.pravesh.user.dto.response;

public record ResidentDirectoryEntry(
        Long residentId,
        String name,
        String phone,
        Long flatId,
        String flatNumber,
        String tower
) {}