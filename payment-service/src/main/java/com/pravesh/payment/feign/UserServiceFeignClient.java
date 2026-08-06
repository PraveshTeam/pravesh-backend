package com.pravesh.payment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceFeignClient {

    // This endpoint returns ResidentContextResponse directly -- NOT wrapped
    // in {success, message, data} like some other internal endpoints. Confirmed
    // via a direct Postman call to /api/internal/residents/{userId}/context.
    @GetMapping("/api/internal/residents/{userId}/context")
    ResidentContextResponse getResidentContext(
            @PathVariable Long userId,
            @RequestHeader("X-Internal-Api-Key") String apiKey);
}
