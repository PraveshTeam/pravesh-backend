package com.pravesh.notification.controller;

import com.pravesh.notification.dto.response.ApiResponse;
import com.pravesh.notification.dto.response.NotificationResponse;
import com.pravesh.notification.security.AuthenticatedUser;
import com.pravesh.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @PostMapping("/api/notifications/mark-read")
    public ApiResponse<Void> markRead(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestBody MarkReadRequest req) {
        if (req != null && req.id() != null) {
            notificationService.markOneRead(caller.userId(), req.id());
        } else {
            notificationService.markAllRead(caller.userId());
        }
        return ApiResponse.ok("Marked as read");
    }

    public record MarkReadRequest(String id) {}
}