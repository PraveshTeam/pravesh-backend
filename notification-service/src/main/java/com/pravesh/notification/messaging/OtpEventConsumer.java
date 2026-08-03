package com.pravesh.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravesh.notification.document.Notification;
import com.pravesh.notification.repository.NotificationRepository;
import com.pravesh.notification.service.EmailService;
import com.pravesh.notification.service.SmsService;
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

		String subject = "Your Pravesh Password Reset Code";
		String emailBody = "Your OTP for password reset is: " + event.otp()
				+ "\n\nThis code expires in 10 minutes. If you didn't request this, ignore this email.";

		boolean sendEmail = "EMAIL".equals(event.channel()) || "BOTH".equals(event.channel());
		boolean sendSms = "SMS".equals(event.channel()) || "BOTH".equals(event.channel());

		if (sendEmail) {
			try {
				emailService.sendPlainEmail(event.email(), subject, emailBody);
			} catch (Exception e) {
				log.warn("Failed to send OTP email to {}: {}", event.email(), e.getMessage());
			}
		}

		if (sendSms && event.phone() != null && !event.phone().isBlank()) {
			try {
				smsService.sendSms(event.phone(), SmsTemplates.otp(event.otp()));
			} catch (Exception e) {
				log.warn("Failed to send OTP SMS to {}: {}", event.phone(), e.getMessage());
			}
		}

		List<String> actualChannels = new java.util.ArrayList<>();
		if (sendEmail)
			actualChannels.add("EMAIL");
		if (sendSms)
			actualChannels.add("SMS");

		Notification notification = Notification.builder().userId(event.userId()).type("OTP_REQUESTED")
				.channel(actualChannels).title("Password Reset Requested")
				.message("A password reset code was sent via " + String.join(" and ", actualChannels).toLowerCase()
						+ ".")
				.sourceEvent(Notification.SourceEvent.builder().eventType("OTP_REQUESTED")
						.correlationId(event.correlationId()).payload(Map.of("expiresInMinutes", 10)).build())
				.isRead(false).createdAt(Instant.now()).build();

		notificationRepository.save(notification);

		log.info("OTP event {} processed — email and SMS dispatch attempted", event.correlationId());
	}
}