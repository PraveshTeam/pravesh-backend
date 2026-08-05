package com.pravesh.notification.repository;

import com.pravesh.notification.document.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsBySourceEventCorrelationId(String correlationId);
    
    Optional<Notification> findByIdAndUserId(String id, Long userId);

    List<Notification> findByUserIdAndIsReadFalse(Long userId);
}