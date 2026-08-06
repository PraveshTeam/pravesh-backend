package com.pravesh.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "validation-service")
public interface ValidationFeignClient {

	@PostMapping("/api/internal/entries/walk-in")
	void logWalkInEntry(@RequestBody WalkInEntryLogRequest request);
}
