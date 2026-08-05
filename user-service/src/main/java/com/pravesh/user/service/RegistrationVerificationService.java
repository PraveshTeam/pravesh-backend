package com.pravesh.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravesh.user.dto.request.SendRegistrationOtpRequest;
import com.pravesh.user.dto.request.VerifyRegistrationOtpRequest;
import com.pravesh.user.entity.OutboxEvent;
import com.pravesh.user.entity.RegistrationVerification;
import com.pravesh.user.entity.enums.OutboxStatus;
import com.pravesh.user.exception.DuplicateResourceException;
import com.pravesh.user.exception.InvalidStateException;
import com.pravesh.user.exception.OtpValidationException;
import com.pravesh.user.repository.OutboxRepository;
import com.pravesh.user.repository.RegistrationVerificationRepository;
import com.pravesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class RegistrationVerificationService {

    private final UserRepository userRepository;
    private final RegistrationVerificationRepository verificationRepository;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    @Transactional
    public void sendOtp(SendRegistrationOtpRequest req) {
        String type = normalizeType(req.contactType());
        String value = req.value() == null ? "" : req.value().trim();

        if (type.equals("EMAIL")) {
            if (!EMAIL_PATTERN.matcher(value).matches()) {
                throw new InvalidStateException("Enter a valid email address");
            }
            if (userRepository.existsByEmail(value)) {
                throw new DuplicateResourceException("Email already registered");
            }
        } else {
            if (!PHONE_PATTERN.matcher(value).matches()) {
                throw new InvalidStateException("Enter a valid 10-digit phone number");
            }
            if (userRepository.existsByPhone(value)) {
                throw new DuplicateResourceException("Phone number already registered");
            }
        }

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        String otpHash = passwordEncoder.encode(otp);

        RegistrationVerification verification = RegistrationVerification.builder()
                .contactType(type)
                .contactValue(value)
                .otpHash(otpHash)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .consumed(false)
                .attemptCount(0)
                .build();
        verification = verificationRepository.save(verification);

        // Same payload shape as the forgot-password OTP event — Notification-Service
        // needs no changes at all. userId is null since no account exists yet.
        String correlationId = UUID.randomUUID().toString();
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("correlationId", correlationId);
        payloadMap.put("userId", null);
        payloadMap.put("email", type.equals("EMAIL") ? value : null);
        payloadMap.put("phone", type.equals("PHONE") ? value : null);
        payloadMap.put("otp", otp);
        payloadMap.put("channel", type.equals("EMAIL") ? "EMAIL" : "SMS");
        payloadMap.put("purpose", "REGISTRATION_VERIFICATION");

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize registration OTP event payload", e);
        }

        // Same eventType as forgot-password ("OTP_REQUESTED") so the existing
        // OutboxPoller routing (-> pravesh.otp.queue) and the existing
        // OtpEventConsumer / email+SMS template are reused as-is.
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType("REGISTRATION_VERIFICATION")
                .aggregateId(verification.getId())
                .eventType("OTP_REQUESTED")
                .payload(payloadJson)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();
        outboxRepository.save(event);
    }

    @Transactional
    public void verifyOtp(VerifyRegistrationOtpRequest req) {
        String type = normalizeType(req.contactType());
        String value = req.value() == null ? "" : req.value().trim();

        RegistrationVerification verification = verificationRepository
                .findTopByContactTypeAndContactValueAndConsumedFalseOrderByCreatedAtDesc(type, value)
                .orElseThrow(() -> new OtpValidationException(
                        "No active OTP request found. Please send a code first."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpValidationException("OTP has expired. Please request a new one.");
        }
        if (verification.getAttemptCount() >= 5) {
            throw new OtpValidationException("Too many incorrect attempts. Please request a new OTP.");
        }
        if (!passwordEncoder.matches(req.otp(), verification.getOtpHash())) {
            verification.setAttemptCount(verification.getAttemptCount() + 1);
            verificationRepository.save(verification);
            throw new OtpValidationException("Incorrect OTP");
        }

        verification.setVerified(true);
        verificationRepository.save(verification);
    }

    private String normalizeType(String contactType) {
        String type = contactType == null ? "" : contactType.trim().toUpperCase();
        if (!type.equals("EMAIL") && !type.equals("PHONE")) {
            throw new InvalidStateException("contactType must be EMAIL or PHONE");
        }
        return type;
    }
}