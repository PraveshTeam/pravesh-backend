package com.pravesh.payment.dto.response;

import java.math.BigDecimal;

public record CheckoutConfigResponse(
        Long paymentOrderId,
        String razorpayKeyId,
        String razorpayOrderId,
        BigDecimal amount,
        String currency
) {}
