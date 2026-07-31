package com.pravesh.sos.controller;

import com.pravesh.sos.dto.request.CreateSosRequest;
import com.pravesh.sos.dto.response.ApiResponse;
import com.pravesh.sos.dto.response.SosAlertResponse;
import com.pravesh.sos.security.AuthenticatedUser;
import com.pravesh.sos.service.SosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sos")
@RequiredArgsConstructor
public class SosController {

    private final SosService sosService;

    @PostMapping
    @PreAuthorize("hasRole('RESIDENT')")
    public ApiResponse<SosAlertResponse> raise(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreateSosRequest req) {
        return ApiResponse.ok("SOS alert raised", sosService.raise(req, caller.userId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('GUARD') or hasRole('SOCIETY_ADMIN')")
    public ApiResponse<List<SosAlertResponse>> active(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Active alerts", sosService.getActiveForSociety(caller.societyId()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('GUARD') or hasRole('SOCIETY_ADMIN')")
    public ApiResponse<SosAlertResponse> updateStatus(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        return ApiResponse.ok("Status updated", sosService.updateStatus(id, body.get("status"), caller.userId()));
    }
}