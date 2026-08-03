package com.pravesh.pass.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/api/internal/residents/{id}/flat-number")
    ApiResponseWrapper<String> getFlatNumber(@PathVariable Long id);
}