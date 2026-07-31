package com.pravesh.validation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ScanRequest(
        @NotBlank(message = "UUID is required")
        String uuid
) {}