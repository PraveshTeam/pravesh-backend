package com.pravesh.payment.dto.response;

import com.pravesh.payment.entity.PaymentPurpose;
import com.pravesh.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Returned for GET history / admin listing endpoints.
public record PaymentOrderResponse(
        Long id,
        PaymentPurpose purpose,
        Long referenceId,
        BigDecimal amount,
        String razorpayOrderId,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {}
