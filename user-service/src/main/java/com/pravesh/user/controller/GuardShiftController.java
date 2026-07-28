package com.pravesh.user.controller;

import com.pravesh.user.dto.request.ShiftCheckinRequest;
import com.pravesh.user.dto.response.ApiResponse;
import com.pravesh.user.dto.response.ShiftCheckinResponse;
import com.pravesh.user.security.AuthenticatedUser;
import com.pravesh.user.service.ShiftCheckinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/guard")
@RequiredArgsConstructor
public class GuardShiftController {

    private final ShiftCheckinService shiftCheckinService;

    @PostMapping("/shift-checkin")
    @PreAuthorize("hasRole('GUARD')")
    public ApiResponse<ShiftCheckinResponse> checkIn(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody ShiftCheckinRequest req) {
        return ApiResponse.ok("Shift started",
                shiftCheckinService.checkIn(caller.userId(), req));
    }
    
    @PostMapping("/shift-checkout")
    @PreAuthorize("hasRole('GUARD')")
    public ApiResponse<Void> checkOut(@AuthenticationPrincipal AuthenticatedUser caller) {
        shiftCheckinService.endShift(caller.userId());
        return ApiResponse.ok("Shift ended");
    }
}