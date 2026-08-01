package com.pravesh.payment.controller;

import com.pravesh.payment.dto.request.CreatePaymentOrderRequest;
import com.pravesh.payment.dto.response.ApiResponse;
import com.pravesh.payment.dto.response.CheckoutConfigResponse;
import com.pravesh.payment.dto.response.PaymentOrderResponse;
import com.pravesh.payment.security.AuthenticatedUser;
import com.pravesh.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    @PreAuthorize("hasRole('RESIDENT')")
    public ApiResponse<CheckoutConfigResponse> createOrder(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreatePaymentOrderRequest req) {
        return ApiResponse.ok("Order created", paymentService.createOrder(req, caller.userId()));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('RESIDENT')")
    public ApiResponse<List<PaymentOrderResponse>> myHistory(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Payment history", paymentService.myHistory(caller.userId()));
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    public ApiResponse<List<PaymentOrderResponse>> allPayments(
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok("All payments", paymentService.allPayments(purpose, status));
    }
}
