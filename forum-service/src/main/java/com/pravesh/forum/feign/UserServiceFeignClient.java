package com.pravesh.forum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

// NOTE: this mirrors notification-service's UserFeignClient, which calls this
// same /api/internal/users/{id}/contact endpoint and expects the response
// WRAPPED in {success, message, data}. Unlike payment-service's separate
// /residents/{id}/context endpoint (confirmed UNWRAPPED via direct Postman
// test), different internal endpoints in this app have different response
// shapes -- verify this one with Postman before trusting it blindly, the
// same way the payment-service mismatch was caught.
@FeignClient(name = "user-service")
public interface UserServiceFeignClient {

    @GetMapping("/api/internal/users/{userId}/contact")
    ApiResponseWrapper<UserContactResponse> getContact(
            @PathVariable Long userId,
            @RequestHeader("X-Internal-Api-Key") String apiKey);
}
