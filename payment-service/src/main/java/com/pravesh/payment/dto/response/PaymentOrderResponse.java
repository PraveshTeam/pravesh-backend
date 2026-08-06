package com.pravesh.payment.dto.response;

import com.pravesh.payment.entity.PaymentPurpose;
import com.pravesh.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentOrderResponse(
        Long id,
        Long residentId,
        String residentName,
        String flatNumber,
        PaymentPurpose purpose,
        Long referenceId,
        BigDecimal amount,
        String razorpayOrderId,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {}
