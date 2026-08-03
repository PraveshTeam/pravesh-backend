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

    // Added for SOS status history: resolves a GUARD or SOCIETY_ADMIN's name
    // when they acknowledge/progress/resolve an alert -- getResidentContext
    // above only works for residents (backed by the Resident entity), so a
    // guard/admin acting on an alert needs this generic contact lookup instead.
    // Confirmed WRAPPED in {success,message,data}, same as payment-service/
    // forum-service's use of this same endpoint.
    @GetMapping("/api/internal/users/{userId}/contact")
    ApiResponseWrapper<UserContactResponse> getContact(
            @PathVariable Long userId,
            @RequestHeader("X-Internal-Api-Key") String apiKey);
}
