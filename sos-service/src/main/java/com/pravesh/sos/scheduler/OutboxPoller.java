package com.pravesh.sos.scheduler;

import com.pravesh.sos.entity.OutboxEvent;
import com.pravesh.sos.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.pravesh.sos.config.RabbitMQConfig.SOS_QUEUE;

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
                rabbitTemplate.convertAndSend(SOS_QUEUE, event.getPayload());
                event.setProcessed(true);
                outboxRepository.save(event);
            } catch (Exception e) {
                // Leave unprocessed — picked up again on the next poll.
                // This is the durability guarantee: even if RabbitMQ is down
                // right now, the event isn't lost, just delayed.
                log.warn("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}