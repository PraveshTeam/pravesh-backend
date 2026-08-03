package com.pravesh.user.controller;

import com.pravesh.user.dto.request.*;
import com.pravesh.user.dto.response.ApiResponse;
import com.pravesh.user.dto.response.AuthResponse;
import com.pravesh.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok("Registration successful. Please complete onboarding.",
                authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Login successful", authService.login(req));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ApiResponse.ok("If an account exists for that email, an OTP has been sent.");
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        String resetToken = authService.verifyOtp(req);
        return ApiResponse.ok("OTP verified", Map.of("resetToken", resetToken));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ApiResponse.ok("Password reset successful. Please log in with your new password.");
    }
}