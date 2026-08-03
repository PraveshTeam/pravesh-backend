package com.pravesh.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationFeignClient {

	@PostMapping("/api/internal/notify/resident-approved")
	void notifyResidentApproved(@RequestBody ResidentApprovedRequest request);

	@PostMapping("/api/internal/notify/society-admin-approved")
	void notifySocietyAdminApproved(@RequestBody SocietyAdminApprovedRequest request);
	
	@PostMapping("/api/internal/notify/guard-credentials")
    void notifyGuardCredentials(@RequestBody GuardCredentialsRequest request);
}