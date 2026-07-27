package com.pravesh.user.messaging;

import com.pravesh.user.entity.OutboxEvent;
import com.pravesh.user.entity.enums.OutboxStatus;
import com.pravesh.user.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending =
                outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : pending) {
            try {
                String routingKey = switch (event.getEventType()) {
                    case "OTP_REQUESTED" -> RabbitMQConfig.OTP_QUEUE;
                    default -> throw new IllegalStateException(
                            "Unknown event type: " + event.getEventType());
                };

                rabbitTemplate.convertAndSend(routingKey, event.getPayload());

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                log.info("Published outbox event {} ({}) to {}",
                        event.getId(), event.getEventType(), routingKey);

            } catch (Exception ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                log.error("Failed to publish outbox event {} (attempt {}): {}",
                        event.getId(), event.getRetryCount(), ex.getMessage());
            }
            outboxRepository.save(event);
        }
    }
}