package com.pravesh.sos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravesh.sos.dto.request.CreateSosRequest;
import com.pravesh.sos.dto.response.SosAlertResponse;
import com.pravesh.sos.entity.*;
import com.pravesh.sos.exception.InvalidStateException;
import com.pravesh.sos.exception.ResourceNotFoundException;
import com.pravesh.sos.feign.ResidentContextResponse;
import com.pravesh.sos.feign.UserServiceFeignClient;
import com.pravesh.sos.repository.OutboxEventRepository;
import com.pravesh.sos.repository.SosAlertRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SosService {

    private static final Logger log = LoggerFactory.getLogger(SosService.class);

    private final SosAlertRepository alertRepository;
    private final OutboxEventRepository outboxRepository;
    private final UserServiceFeignClient userServiceFeignClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${pravesh.internal.api-key}")
    private String internalApiKey;

    @Transactional
    public SosAlertResponse raise(CreateSosRequest req, Long residentUserId) {
        ResidentContextResponse ctx = userServiceFeignClient.getResidentContext(residentUserId, internalApiKey);
        if (ctx == null || ctx.flatId() == null) {
            throw new InvalidStateException("You must have a flat assigned to raise an SOS alert");
        }

        SosAlert alert = SosAlert.builder()
                .residentUserId(residentUserId)
                .flatId(ctx.flatId())
                .societyId(ctx.societyId())
                .category(SosCategory.valueOf(req.category().toUpperCase()))
                .description(req.description())
                .build();
        alert = alertRepository.save(alert);

        writeOutboxEvent("SOS_RAISED", alert, ctx.name(), ctx.phone(), ctx.flatNumber());

        return toResponse(alert, ctx);
    }

    public List<SosAlertResponse> getActiveForSociety(Long societyId) {
        return alertRepository.findBySocietyIdAndStatusNotOrderByCreatedAtDesc(societyId, SosStatus.RESOLVED)
                .stream()
                .map(a -> toResponse(a, userServiceFeignClient.getResidentContext(a.getResidentUserId(), internalApiKey)))
                .toList();
    }

    @Transactional
    public SosAlertResponse updateStatus(Long alertId, String newStatus, Long callerId) {
        SosAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        SosStatus target = SosStatus.valueOf(newStatus.toUpperCase());
        validateTransition(alert.getStatus(), target);

        alert.setStatus(target);
        if (target == SosStatus.ACKNOWLEDGED) {
            alert.setAcknowledgedBy(callerId);
            alert.setAcknowledgedAt(LocalDateTime.now());
        } else if (target == SosStatus.RESOLVED) {
            alert.setResolvedAt(LocalDateTime.now());
        }
        alertRepository.save(alert);

        ResidentContextResponse ctx = userServiceFeignClient.getResidentContext(alert.getResidentUserId(), internalApiKey);
        writeOutboxEvent("SOS_STATUS_UPDATED", alert, ctx != null ? ctx.name() : "Unknown", null,
                ctx != null ? ctx.flatNumber() : null);

        return toResponse(alert, ctx);
    }

    private void writeOutboxEvent(String eventType, SosAlert alert, String residentName, String residentPhone,
                                   String flatNumber) {
        // The outbox is now the ONLY real-time path — sos-service has no
        // reachable WebSocket broker of its own (the frontend connects through
        // the Gateway to notification-service's broker instead). Every alert
        // and status change goes through here, gets picked up by the poller,
        // published to RabbitMQ, and notification-service both SMSes (on raise)
        // and pushes the live WebSocket update.
        try {
            var payload = new java.util.HashMap<String, Object>();
            payload.put("eventType", eventType);
            payload.put("id", alert.getId());
            payload.put("residentName", residentName);
            payload.put("residentPhone", residentPhone);
            payload.put("flatNumber", flatNumber);
            payload.put("category", alert.getCategory().name());
            payload.put("description", alert.getDescription());
            payload.put("status", alert.getStatus().name());
            payload.put("societyId", alert.getSocietyId());

            String json = mapper.writeValueAsString(payload);
            outboxRepository.save(OutboxEvent.builder()
                    .aggregateId(alert.getId())
                    .eventType(eventType)
                    .payload(json)
                    .build());
        } catch (Exception e) {
            log.error("Failed to write outbox event {} for alert {}: {}", eventType, alert.getId(), e.getMessage());
            throw new RuntimeException("Failed to record SOS event", e);
        }
    }

    private void validateTransition(SosStatus current, SosStatus target) {
        boolean valid = switch (current) {
            case ACTIVE -> target == SosStatus.ACKNOWLEDGED;
            case ACKNOWLEDGED -> target == SosStatus.HELP_ON_THE_WAY;
            case HELP_ON_THE_WAY -> target == SosStatus.RESOLVED;
            case RESOLVED -> false;
        };
        if (!valid) {
            throw new InvalidStateException("Cannot move from " + current + " to " + target);
        }
    }

    private SosAlertResponse toResponse(SosAlert a, ResidentContextResponse ctx) {
        return new SosAlertResponse(
                a.getId(),
                ctx != null ? ctx.name() : "Unknown",
                ctx != null ? ctx.flatNumber() : "—",
                ctx != null ? ctx.phone() : null,
                a.getCategory(), a.getDescription(), a.getStatus(),
                a.getCreatedAt(), a.getAcknowledgedAt(), a.getResolvedAt());
    }
}