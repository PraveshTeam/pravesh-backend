package com.pravesh.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravesh.payment.dto.request.CreatePaymentOrderRequest;
import com.pravesh.payment.dto.response.CheckoutConfigResponse;
import com.pravesh.payment.dto.response.PaymentOrderResponse;
import com.pravesh.payment.entity.OutboxEvent;
import com.pravesh.payment.entity.PaymentOrder;
import com.pravesh.payment.entity.PaymentPurpose;
import com.pravesh.payment.entity.PaymentStatus;
import com.pravesh.payment.exception.InvalidStateException;
import com.pravesh.payment.exception.ResourceNotFoundException;
import com.pravesh.payment.feign.ResidentContextResponse;
import com.pravesh.payment.feign.UserServiceFeignClient;
import com.pravesh.payment.repository.OutboxEventRepository;
import com.pravesh.payment.repository.PaymentOrderRepository;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentOrderRepository orderRepository;
    private final OutboxEventRepository outboxRepository;
    private final RazorpayGatewayService razorpayGatewayService;
    private final UserServiceFeignClient userServiceFeignClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${pravesh.internal.api-key}")
    private String internalApiKey;

    @Transactional
    public CheckoutConfigResponse createOrder(CreatePaymentOrderRequest req, Long residentId, Long societyId) {
        if (societyId == null) {
            throw new InvalidStateException("Could not determine your society. Please log in again.");
        }

        PaymentPurpose purpose;
        try {
            purpose = PaymentPurpose.valueOf(req.purpose().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStateException("Unknown payment purpose: " + req.purpose());
        }

        if (purpose != PaymentPurpose.MAINTENANCE && req.referenceId() == null) {
            throw new InvalidStateException("referenceId is required for " + purpose + " payments");
        }

        String receiptRef = "pravesh-" + residentId + "-" + System.currentTimeMillis();
        String razorpayOrderId;
        try {
            razorpayOrderId = razorpayGatewayService.createOrder(req.amount(), receiptRef);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for resident {}: {}", residentId, e.getMessage());
            throw new InvalidStateException("Could not initiate payment. Please try again.");
        }

        PaymentOrder order = PaymentOrder.builder()
                .residentId(residentId)
                .societyId(societyId)
                .purpose(purpose)
                .referenceId(req.referenceId())
                .amount(req.amount())
                .razorpayOrderId(razorpayOrderId)
                .status(PaymentStatus.PENDING)
                .webhookVerified(false)
                .build();

        order = orderRepository.save(order);

        return new CheckoutConfigResponse(
                order.getId(),
                razorpayGatewayService.getKeyId(),
                razorpayOrderId,
                order.getAmount(),
                "INR");
    }

    public List<PaymentOrderResponse> myHistory(Long residentId) {
        return orderRepository.findByResidentIdOrderByCreatedAtDesc(residentId)
                .stream().map(o -> toResponse(o, null)).toList();
    }

    public List<PaymentOrderResponse> allPayments(String purpose, String status, Long adminSocietyId) {
        List<PaymentOrder> orders = orderRepository.findBySocietyIdOrderByCreatedAtDesc(adminSocietyId).stream()
                .filter(o -> purpose == null || o.getPurpose().name().equalsIgnoreCase(purpose))
                .filter(o -> status == null || o.getStatus().name().equalsIgnoreCase(status))
                .toList();

        Set<Long> distinctResidentIds = orders.stream()
                .map(PaymentOrder::getResidentId)
                .collect(Collectors.toSet());

        Map<Long, ResidentContextResponse> residentContextById = new HashMap<>();
        for (Long residentId : distinctResidentIds) {
            try {
                ResidentContextResponse ctx = userServiceFeignClient
                        .getResidentContext(residentId, internalApiKey);
                if (ctx != null) {
                    residentContextById.put(residentId, ctx);
                }
            } catch (Exception e) {
                log.warn("Could not resolve resident context for {} while building admin payment list: {}",
                        residentId, e.getMessage(), e);
            }
        }

        return orders.stream()
                .map(o -> toResponse(o, residentContextById.get(o.getResidentId())))
                .toList();
    }

    @Transactional
    public void handlePaymentCaptured(String rawBody) {
        JSONObject payload = new JSONObject(rawBody);
        JSONObject paymentEntity = payload
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayOrderId = paymentEntity.getString("order_id");

        PaymentOrder order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No internal order found for Razorpay order " + razorpayOrderId));

        if (order.isWebhookVerified() && order.getStatus() == PaymentStatus.PAID) {
            log.info("Webhook for order {} already processed — skipping (idempotent no-op)", order.getId());
            return;
        }

        order.setStatus(PaymentStatus.PAID);
        order.setWebhookVerified(true);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        writeReceiptOutboxEvent(order);
    }

    private void writeReceiptOutboxEvent(PaymentOrder order) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventType", "PAYMENT_RECEIPT_READY");
            payload.put("paymentOrderId", order.getId());
            payload.put("residentId", order.getResidentId());
            payload.put("amount", order.getAmount());
            payload.put("purpose", order.getPurpose().name());
            payload.put("paidAt", order.getPaidAt().toString());

            String json = mapper.writeValueAsString(payload);
            outboxRepository.save(OutboxEvent.builder()
                    .aggregateId(order.getId())
                    .eventType("PAYMENT_RECEIPT_READY")
                    .payload(json)
                    .build());
        } catch (Exception e) {
            log.error("Failed to write payment-receipt outbox event for order {}: {}",
                    order.getId(), e.getMessage());
            throw new RuntimeException("Failed to record payment receipt event", e);
        }
    }

    private PaymentOrderResponse toResponse(PaymentOrder o, ResidentContextResponse ctx) {
        return new PaymentOrderResponse(
                o.getId(),
                o.getResidentId(),
                ctx != null ? ctx.name() : null,
                ctx != null ? ctx.flatNumber() : null,
                o.getPurpose(),
                o.getReferenceId(),
                o.getAmount(),
                o.getRazorpayOrderId(),
                o.getStatus(),
                o.getCreatedAt(),
                o.getPaidAt());
    }
}
