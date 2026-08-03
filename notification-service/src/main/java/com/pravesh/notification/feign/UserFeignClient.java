package com.pravesh.notification.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/api/internal/users/{id}/contact")
    ApiResponseWrapper<UserContactResponse> getContact(@PathVariable Long id);

    @GetMapping("/api/internal/societies/{societyId}/emergency-contact")
    ApiResponseWrapper<EmergencyContactResponse> getEmergencyContact(@PathVariable Long societyId);
}