package com.pravesh.sos.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceFeignClient {

    @GetMapping("/api/internal/residents/{userId}/context")
    ResidentContextResponse getResidentContext(
            @PathVariable Long userId,
            @RequestHeader("X-Internal-Api-Key") String apiKey);
}