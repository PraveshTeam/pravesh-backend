package com.pravesh.pass.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "notification-service")
public interface NotificationFeignClient {

    @PostMapping("/api/internal/notify/pass-revoked")
    void notifyPassRevoked(@RequestBody PassRevokedRequest request);

    @PostMapping("/api/internal/notify/pass-created")
    void notifyPassCreated(@RequestBody PassCreatedRequest request);
}