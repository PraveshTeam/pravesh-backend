package com.pravesh.pass.controller;

import com.pravesh.pass.dto.response.ApiResponse;
import com.pravesh.pass.dto.response.PassValidationResponse;
import com.pravesh.pass.service.PassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/passes")
@RequiredArgsConstructor
public class InternalPassController {

    private final PassService passService;

    @PostMapping("/validate-and-consume/{uuid}")
    public ApiResponse<PassValidationResponse> validateAndConsume(
            @PathVariable String uuid,
            @RequestParam Long callerSocietyId) {
        return ApiResponse.ok("Validation result",
                passService.validateAndConsume(uuid, callerSocietyId));
    }
}