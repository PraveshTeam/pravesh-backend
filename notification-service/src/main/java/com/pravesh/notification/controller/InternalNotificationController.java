package com.pravesh.notification.controller;

<<<<<<< Updated upstream
=======
import com.pravesh.notification.dto.request.DisplacementNotifyRequest;
>>>>>>> Stashed changes
import com.pravesh.notification.dto.request.GateEntryNotifyRequest;
import com.pravesh.notification.dto.request.GuardCredentialsRequest;
import com.pravesh.notification.dto.request.PassCreatedRequest;
import com.pravesh.notification.dto.request.PassRevokedRequest;
<<<<<<< Updated upstream
=======
import com.pravesh.notification.dto.request.RelocationApprovedRequest;
>>>>>>> Stashed changes
import com.pravesh.notification.dto.request.ResidentApprovedRequest;
import com.pravesh.notification.dto.request.SocietyAdminApprovedRequest;
import com.pravesh.notification.dto.request.VisitorEnteredRequest;
import com.pravesh.notification.dto.response.ApiResponse;
import com.pravesh.notification.service.NotificationService;
<<<<<<< Updated upstream
=======
import com.pravesh.notification.service.SmsService;

>>>>>>> Stashed changes
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/notify")
@RequiredArgsConstructor
public class InternalNotificationController {

	private final NotificationService notificationService;
<<<<<<< Updated upstream
=======
	private final SmsService smsService;
>>>>>>> Stashed changes

	@PostMapping("/visitor-entered")
	public ApiResponse<Void> visitorEntered(@RequestBody VisitorEnteredRequest req) {
		notificationService.handleVisitorEntered(req);
		return ApiResponse.ok("Notification dispatched");
	}

	@PostMapping("/pass-created")
	public ApiResponse<Void> passCreated(@RequestBody PassCreatedRequest req) {
		notificationService.handlePassCreated(req);
		return ApiResponse.ok("Notification dispatched");
	}

	@PostMapping("/pass-revoked")
	public ApiResponse<Void> passRevoked(@RequestBody PassRevokedRequest req) {
		notificationService.handlePassRevoked(req);
		return ApiResponse.ok("Notification dispatched");
	}

	@PostMapping("/resident-approved")
	public ApiResponse<Void> residentApproved(@RequestBody ResidentApprovedRequest req) {
		notificationService.handleResidentApproved(req);
		return ApiResponse.ok("Notification dispatched");
	}

	@PostMapping("/society-admin-approved")
	public ApiResponse<Void> societyAdminApproved(@RequestBody SocietyAdminApprovedRequest req) {
		notificationService.handleSocietyAdminApproved(req);
		return ApiResponse.ok("Notification dispatched");
	}
	
	@PostMapping("/guard-credentials")
    public ApiResponse<Void> guardCredentials(@RequestBody GuardCredentialsRequest req) {
        notificationService.handleGuardCredentials(req);
        return ApiResponse.ok("Notification dispatched");
    }
	
	@PostMapping("/gate-entry-request")
    public ApiResponse<Void> gateEntryRequest(@RequestBody GateEntryNotifyRequest req) {
        notificationService.handleGateEntryRequest(req);
        return ApiResponse.ok("Notification dispatched");
    }
<<<<<<< Updated upstream
=======
	
	@PostMapping("/flat-displacement")
    public ApiResponse<Void> flatDisplacement(@RequestBody DisplacementNotifyRequest req) {
		smsService.handleFlatDisplacement(req);
        return ApiResponse.ok("Notification dispatched");
    }
	
	@PostMapping("/relocation-approved")
	public ApiResponse<Void> relocationApproved(@RequestBody RelocationApprovedRequest req) {
		notificationService.handleRelocationApproved(req);
		return ApiResponse.ok("Notification dispatched");
	}
>>>>>>> Stashed changes
}