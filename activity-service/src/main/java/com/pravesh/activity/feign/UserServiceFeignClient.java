package com.pravesh.activity.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceFeignClient {

    // Confirmed WRAPPED -- used for general author-name resolution (posts, comments).
    @GetMapping("/api/internal/users/{userId}/contact")
    ApiResponseWrapper<UserContactResponse> getContact(
            @PathVariable Long userId,
            @RequestHeader("X-Internal-Api-Key") String apiKey);

    // Confirmed UNWRAPPED -- used specifically for the trip Participants panel,
    // since it's the only endpoint that also returns flatNumber.
    @GetMapping("/api/internal/residents/{userId}/context")
    ResidentContextResponse getResidentContext(
            @PathVariable Long userId,
            @RequestHeader("X-Internal-Api-Key") String apiKey);
}
