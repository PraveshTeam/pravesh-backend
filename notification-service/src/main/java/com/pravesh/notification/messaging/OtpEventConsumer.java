package com.pravesh.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravesh.notification.document.Notification;
import com.pravesh.notification.repository.NotificationRepository;
import com.pravesh.notification.service.EmailService;
import com.pravesh.notification.service.SmsService;
<<<<<<< Updated upstream
=======
import com.pravesh.notification.util.EmailTemplates;
>>>>>>> Stashed changes
import com.pravesh.notification.util.SmsTemplates;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OtpEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(OtpEventConsumer.class);
<<<<<<< Updated upstream
=======
	private static final int OTP_EXPIRY_MINUTES = 10;
	private static final String REGISTRATION_VERIFICATION = "REGISTRATION_VERIFICATION";
>>>>>>> Stashed changes

	private final NotificationRepository notificationRepository;
	private final EmailService emailService;
	private final SmsService smsService;
	private final ObjectMapper objectMapper;

	@RabbitListener(queues = RabbitMQConfig.OTP_QUEUE)
	public void handleOtpEvent(String rawPayload) {
		OtpEventPayload event;
		try {
			event = objectMapper.readValue(rawPayload, OtpEventPayload.class);
		} catch (Exception e) {
			log.error("Failed to parse OTP event payload: {}", rawPayload, e);
			return; // malformed message — don't retry indefinitely, goes to DLQ via broker config
					// if configured for parse failures
		}

		// Idempotency check — RabbitMQ's at-least-once delivery can redeliver
		if (notificationRepository.existsBySourceEventCorrelationId(event.correlationId())) {
			log.info("OTP event {} already processed — skipping duplicate", event.correlationId());
			return;
		}

<<<<<<< Updated upstream
		String subject = "Your Pravesh Password Reset Code";
		String emailBody = "Your OTP for password reset is: " + event.otp()
				+ "\n\nThis code expires in 10 minutes. If you didn't request this, ignore this email.";
=======
		// purpose is missing on any already-queued/older messages — treat those as
		// password-reset (the original, only behavior) so nothing breaks.
		boolean isRegistration = REGISTRATION_VERIFICATION.equals(event.purpose());

		String subject = isRegistration
				? "Verify Your Details — Pravesh"
				: "Your Pravesh Password Reset Code";
		String emailHtml = isRegistration
				? EmailTemplates.otpRegistrationVerification(event.otp(), OTP_EXPIRY_MINUTES)
				: EmailTemplates.otpPasswordReset(event.otp(), OTP_EXPIRY_MINUTES);
		String smsText = isRegistration
				? SmsTemplates.registrationOtp(event.otp())
				: SmsTemplates.otp(event.otp());
>>>>>>> Stashed changes

		boolean sendEmail = "EMAIL".equals(event.channel()) || "BOTH".equals(event.channel());
		boolean sendSms = "SMS".equals(event.channel()) || "BOTH".equals(event.channel());

		if (sendEmail) {
			try {
<<<<<<< Updated upstream
				emailService.sendPlainEmail(event.email(), subject, emailBody);
=======
				emailService.sendHtmlEmail(event.email(), subject, emailHtml);
>>>>>>> Stashed changes
			} catch (Exception e) {
				log.warn("Failed to send OTP email to {}: {}", event.email(), e.getMessage());
			}
		}

		if (sendSms && event.phone() != null && !event.phone().isBlank()) {
			try {
<<<<<<< Updated upstream
				smsService.sendSms(event.phone(), SmsTemplates.otp(event.otp()));
=======
				smsService.sendSms(event.phone(), smsText);
>>>>>>> Stashed changes
			} catch (Exception e) {
				log.warn("Failed to send OTP SMS to {}: {}", event.phone(), e.getMessage());
			}
		}

		List<String> actualChannels = new java.util.ArrayList<>();
		if (sendEmail)
			actualChannels.add("EMAIL");
		if (sendSms)
			actualChannels.add("SMS");

<<<<<<< Updated upstream
		Notification notification = Notification.builder().userId(event.userId()).type("OTP_REQUESTED")
				.channel(actualChannels).title("Password Reset Requested")
				.message("A password reset code was sent via " + String.join(" and ", actualChannels).toLowerCase()
						+ ".")
				.sourceEvent(Notification.SourceEvent.builder().eventType("OTP_REQUESTED")
						.correlationId(event.correlationId()).payload(Map.of("expiresInMinutes", 10)).build())
=======
		String notifTitle = isRegistration ? "Verification Code Sent" : "Password Reset Requested";
		String notifMessage = (isRegistration ? "A verification code was sent via " : "A password reset code was sent via ")
				+ String.join(" and ", actualChannels).toLowerCase() + ".";

		Notification notification = Notification.builder().userId(event.userId()).type("OTP_REQUESTED")
				.channel(actualChannels).title(notifTitle)
				.message(notifMessage)
				.sourceEvent(Notification.SourceEvent.builder().eventType("OTP_REQUESTED")
						.correlationId(event.correlationId())
						.payload(Map.of("expiresInMinutes", OTP_EXPIRY_MINUTES, "purpose",
								isRegistration ? REGISTRATION_VERIFICATION : "PASSWORD_RESET"))
						.build())
>>>>>>> Stashed changes
				.isRead(false).createdAt(Instant.now()).build();

		notificationRepository.save(notification);

<<<<<<< Updated upstream
		log.info("OTP event {} processed — email and SMS dispatch attempted", event.correlationId());
=======
		log.info("OTP event {} ({}) processed — email and SMS dispatch attempted", event.correlationId(),
				isRegistration ? REGISTRATION_VERIFICATION : "PASSWORD_RESET");
>>>>>>> Stashed changes
	}
}