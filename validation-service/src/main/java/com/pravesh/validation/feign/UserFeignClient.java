package com.pravesh.validation.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/api/internal/guards/{guardUserId}/shift-status")
    ApiResponseWrapper<ShiftStatusResponse> getShiftStatus(@PathVariable Long guardUserId);
}