package com.pravesh.notification.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "notifications")
@CompoundIndex(name = "userId_createdAt_idx", def = "{'userId': 1, 'createdAt': -1}")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    private String id;

    private Long userId;

    private String type; // VISITOR_ENTERED, OTP_REQUESTED, SOS_TRIGGERED, PAYMENT_RECEIPT

    private List<String> channel; // EMAIL, SMS, WEBSOCKET

    private String title;

    private String message;

    private SourceEvent sourceEvent;

    private boolean isRead;

    private Instant createdAt;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SourceEvent {
        private String eventType;
        private String correlationId;
        private Map<String, Object> payload;
    }
}