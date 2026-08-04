package com.pravesh.notification.service;

import com.pravesh.notification.document.Notification;
import com.pravesh.notification.dto.request.GateEntryNotifyRequest;
import com.pravesh.notification.dto.request.GuardCredentialsRequest;
import com.pravesh.notification.dto.request.PassCreatedRequest;
import com.pravesh.notification.dto.request.PassRevokedRequest;
import com.pravesh.notification.dto.request.RelocationApprovedRequest;
import com.pravesh.notification.dto.request.ResidentApprovedRequest;
import com.pravesh.notification.dto.request.SocietyAdminApprovedRequest;
import com.pravesh.notification.dto.request.VisitorEnteredRequest;
import com.pravesh.notification.dto.response.NotificationResponse;
import com.pravesh.notification.dto.response.WebSocketNotificationPayload;
import com.pravesh.notification.feign.ApiResponseWrapper;
import com.pravesh.notification.feign.UserContactResponse;
import com.pravesh.notification.feign.UserFeignClient;
import com.pravesh.notification.repository.NotificationRepository;
import com.pravesh.notification.util.DateFormatUtil;
import com.pravesh.notification.util.EmailTemplates;
import com.pravesh.notification.util.SmsTemplates;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
	
	@org.springframework.beans.factory.annotation.Value("${pravesh.app-link}")
    private String appLink;

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRepository notificationRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final EmailService emailService;
	private final SmsService smsService;
	private final UserFeignClient userFeignClient;

	public void handleVisitorEntered(VisitorEnteredRequest req) {
		messagingTemplate.convertAndSend("/topic/flat/" + req.residentId() + "/notifications",
				new WebSocketNotificationPayload("VISITOR_ENTERED", req.visitorName(), req.scannedAt(),
						req.gateName()));

		UserContactResponse contact = fetchContact(req.residentId());
		if (contact != null) {
			safeSendSms(contact.phone(),
					SmsTemplates.visitorEntered(req.visitorName(), req.gateName(), req.scannedAt()));
			safeSendHtmlEmail(contact.email(), "Visitor Entered",
					EmailTemplates.visitorEntered(contact.name(), req.visitorName(), req.gateName(), req.scannedAt()));
		}

		save(req.residentId(), "VISITOR_ENTERED", List.of("WEBSOCKET", "EMAIL", "SMS"), "Visitor Entered",
				req.visitorName() + " entered via " + req.gateName() + ".");
	}

	public void handlePassCreated(PassCreatedRequest req) {
		UserContactResponse contact = fetchContact(req.residentId());
		String formattedFrom = DateFormatUtil.format(req.validFrom());
		String formattedUntil = DateFormatUtil.format(req.validUntil());

		if (contact != null) {
			if (req.qrBase64() != null) {
				try {
					emailService.sendHtmlEmailWithInlineImage(
							contact.email(), "Visitor Pass Created", EmailTemplates.passCreated(contact.name(),
									req.flatNumber(), req.visitorName(), req.passUuid(), formattedFrom, formattedUntil),
							req.qrBase64(), "qrImage");
				} catch (Exception e) {
					log.warn("Failed to send pass-created email with QR to {}: {}", contact.email(), e.getMessage());
				}
			} else {
				safeSendHtmlEmail(contact.email(), "Visitor Pass Created", EmailTemplates.passCreated(contact.name(),
						req.flatNumber(), req.visitorName(), req.passUuid(), formattedFrom, formattedUntil));
			}
			safeSendSms(contact.phone(),
					SmsTemplates.passCreated(req.visitorName(), req.validFrom(), req.validUntil()));
		}

		save(req.residentId(), "PASS_CREATED", List.of("EMAIL", "SMS"), "Visitor Pass Created",
				"Pass created for " + req.visitorName() + ".");
	}

	public void handlePassRevoked(PassRevokedRequest req) {
		UserContactResponse contact = fetchContact(req.residentId());
		if (contact != null) {
			safeSendHtmlEmail(contact.email(), "Visitor Pass Revoked",
					EmailTemplates.passRevoked(contact.name(), req.visitorName(), req.passUuid()));
			safeSendSms(contact.phone(), SmsTemplates.passRevoked(req.visitorName()));
		}

		save(req.residentId(), "PASS_REVOKED", List.of("EMAIL", "SMS"), "Visitor Pass Revoked",
				"Pass for " + req.visitorName() + " was revoked.");
	}

	public List<NotificationResponse> getMyNotifications(Long userId) {
		return notificationRepository
				.findByUserIdOrderByCreatedAtDesc(userId).stream().map(n -> new NotificationResponse(n.getId(),
						n.getType(), n.getChannel(), n.getTitle(), n.getMessage(), n.isRead(), n.getCreatedAt()))
				.toList();
	}

	private void save(Long userId, String type, List<String> channel, String title, String message) {
		notificationRepository.save(Notification.builder().userId(userId).type(type).channel(channel).title(title)
				.message(message).isRead(false).createdAt(Instant.now()).build());
	}

	private UserContactResponse fetchContact(Long userId) {
		try {
			ApiResponseWrapper<UserContactResponse> wrapper = userFeignClient.getContact(userId);
			return wrapper.data();
		} catch (Exception e) {
			log.warn("Failed to fetch contact for userId {}: {}", userId, e.getMessage());
			return null;
		}
	}

	private void safeSendHtmlEmail(String email, String subject, String html) {
		try {
			emailService.sendHtmlEmail(email, subject, html);
		} catch (Exception e) {
			log.warn("Failed to send email to {}: {}", email, e.getMessage());
		}
	}

	private void safeSendSms(String phone, String body) {
		try {
			smsService.sendSms(phone, body);
		} catch (Exception e) {
			log.warn("Failed to send SMS to {}: {}", phone, e.getMessage());
		}
	}

	public void handleResidentApproved(ResidentApprovedRequest req) {
		UserContactResponse contact = fetchContact(req.residentId());
		if (contact != null) {
			safeSendHtmlEmail(contact.email(), "Welcome to Pravesh 🎉",
					EmailTemplates.residentApproved(contact.name(), req.flatNumber(), appLink));
		}
		save(req.residentId(), "RESIDENT_APPROVED", List.of("EMAIL"), "Welcome to Pravesh",
				"Your onboarding request was approved.");
	}

	public void handleSocietyAdminApproved(SocietyAdminApprovedRequest req) {
		UserContactResponse contact = fetchContact(req.adminId());
		if (contact != null) {
			safeSendHtmlEmail(contact.email(), "Welcome to Pravesh 🎉",
					EmailTemplates.societyAdminApproved(contact.name(), req.societyName(), appLink));
		}
		save(req.adminId(), "SOCIETY_ADMIN_APPROVED", List.of("EMAIL"), "Welcome to Pravesh",
				"Your society registration was approved.");
	}
	
	public void handleGuardCredentials(GuardCredentialsRequest req) {
        String smsBody = "Pravesh: Your guard login for " + req.gateName()
                + " — username: guard." + req.phone() + "@pravesh.local, temp password: "
                + req.tempPassword() + ". Please log in and note this down.";

        safeSendSms(req.phone(), smsBody);
    }
	
	public void handleGateEntryRequest(GateEntryNotifyRequest req) {
        String smsBody = "Pravesh: " + req.visitorName() + " is at the gate for flat "
                + req.flatNumber() + ". Open the Pravesh app to approve or deny entry.";

        safeSendSms(req.residentPhone(), smsBody);
    }
	
	public void markOneRead(Long userId, String notificationId) {
		notificationRepository.findByIdAndUserId(notificationId, userId)
				.ifPresent(n -> {
					n.setRead(true);
					notificationRepository.save(n);
				});
	}
	
	public void handleRelocationApproved(RelocationApprovedRequest req) {
		UserContactResponse contact = fetchContact(req.residentId());
		if (contact != null) {
			safeSendHtmlEmail(contact.email(), "Your Relocation Was Approved ✅",
					EmailTemplates.relocationApproved(contact.name(),
							req.oldFlatNumber(), req.oldSocietyName(),
							req.newFlatNumber(), req.newTower(), req.newSocietyName(), appLink));
		}
		save(req.residentId(), "RELOCATION_APPROVED", List.of("EMAIL"), "Relocation Approved",
				"You've been moved to flat " + req.newFlatNumber() + " in " + req.newSocietyName() + ".");
	}

	public void markAllRead(Long userId) {
		List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
		unread.forEach(n -> n.setRead(true));
		notificationRepository.saveAll(unread);
	}
}