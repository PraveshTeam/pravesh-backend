package com.pravesh.payment.scheduler;

import com.pravesh.payment.config.RabbitMQConfig;
import com.pravesh.payment.entity.OutboxEvent;
import com.pravesh.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc();
        for (OutboxEvent event : pending) {
            try {
                rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_RECEIPT_QUEUE, event.getPayload());
                event.setProcessed(true);
                outboxRepository.save(event);
            } catch (Exception e) {
                log.warn("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
