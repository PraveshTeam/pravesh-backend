package com.pravesh.validation.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "pass-service")
public interface PassFeignClient {

    @PostMapping("/api/internal/passes/validate-and-consume/{uuid}")
    ApiResponseWrapper<PassValidationResponse> validateAndConsume(
            @PathVariable String uuid,
            @RequestParam("callerSocietyId") Long callerSocietyId);
}