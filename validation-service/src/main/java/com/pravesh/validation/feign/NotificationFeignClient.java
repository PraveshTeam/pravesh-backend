package com.pravesh.validation.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationFeignClient {

    @PostMapping("/api/internal/notify/visitor-entered")
    void notifyVisitorEntered(@RequestBody VisitorEnteredRequest request);
}