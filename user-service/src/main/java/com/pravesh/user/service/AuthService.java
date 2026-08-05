package com.pravesh.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravesh.user.dto.request.*;
import com.pravesh.user.dto.response.AuthResponse;
import com.pravesh.user.entity.*;
import com.pravesh.user.entity.enums.OutboxStatus;
import com.pravesh.user.entity.enums.Role;
import com.pravesh.user.entity.enums.VerificationStatus;
import com.pravesh.user.exception.*;
import com.pravesh.user.repository.*;
import com.pravesh.user.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final SocietyAdminRepository societyAdminRepository;
    private final GateRepository gateRepository;
    private final GuardRepository guardRepository;
    private final FlatRepository flatRepository;
    private final RegistrationVerificationRepository registrationVerificationRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String requestedRole = (req.role() == null || req.role().isBlank())
                ? "RESIDENT" : req.role().toUpperCase();

        if (!requestedRole.equals("RESIDENT") && !requestedRole.equals("SOCIETY_ADMIN")) {
            throw new InvalidStateException(
                    "Self-registration is only permitted for residents and society admins");
        }

        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByPhone(req.phone())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        // Both contacts must have been OTP-verified via /api/auth/register/send-otp
        // + /api/auth/register/verify-otp before an account can actually be created.
        RegistrationVerification emailVerification = registrationVerificationRepository
                .findTopByContactTypeAndContactValueAndVerifiedTrueAndConsumedFalseOrderByCreatedAtDesc(
                        "EMAIL", req.email())
                .orElseThrow(() -> new InvalidStateException(
                        "Please verify your email before registering."));
        RegistrationVerification phoneVerification = registrationVerificationRepository
                .findTopByContactTypeAndContactValueAndVerifiedTrueAndConsumedFalseOrderByCreatedAtDesc(
                        "PHONE", req.phone())
                .orElseThrow(() -> new InvalidStateException(
                        "Please verify your phone number before registering."));

        Role role = Role.valueOf(requestedRole);

        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .phone(req.phone())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(role)
                .state(req.state())
                .isActive(true)
                .build();
        user = userRepository.save(user);

        String verificationStatus;

        if (role == Role.RESIDENT) {
            Resident resident = Resident.builder()
                    .user(user)
                    .flatId(null)
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            residentRepository.save(resident);
            verificationStatus = resident.getVerificationStatus().name();
        } else {
            SocietyAdmin admin = SocietyAdmin.builder()
                    .user(user)
                    .societyId(null)
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            societyAdminRepository.save(admin);
            verificationStatus = admin.getVerificationStatus().name();
        }

        emailVerification.setConsumed(true);
        phoneVerification.setConsumed(true);
        registrationVerificationRepository.save(emailVerification);
        registrationVerificationRepository.save(phoneVerification);

        String token = jwtUtil.generateToken(user, verificationStatus, null);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), verificationStatus);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!user.isActive()) {
            throw new InvalidStateException("This account has been deactivated");
        }

        String verificationStatus = null;
        Long societyId = null;

        if (user.getRole() == Role.RESIDENT) {
            var resident = residentRepository.findById(user.getId()).orElse(null);
            if (resident != null) {
                verificationStatus = resident.getVerificationStatus().name();
                if (resident.getFlatId() != null) {
                    societyId = flatRepository.findById(resident.getFlatId())
                            .map(Flat::getSocietyId)
                            .orElse(null);
                }
            }
        } else if (user.getRole() == Role.SOCIETY_ADMIN) {
            var admin = societyAdminRepository.findById(user.getId()).orElse(null);
            if (admin != null) {
                verificationStatus = admin.getVerificationStatus().name();
                societyId = admin.getSocietyId();
            }
        } else if (user.getRole() == Role.GUARD) {
            societyId = guardRepository.findById(user.getId())
                    .map(g -> gateRepository.findById(g.getGateId())
                            .map(Gate::getSocietyId)
                            .orElse(null))
                    .orElse(null);
        }

        String token = jwtUtil.generateToken(user, verificationStatus, societyId);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), verificationStatus);
    }
    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with that email"));

        String channel = (req.channel() == null || req.channel().isBlank())
                ? "BOTH" : req.channel().toUpperCase();

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        String otpHash = passwordEncoder.encode(otp);

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .userId(user.getId())
                .otpHash(otpHash)
                .channel(channel)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .attemptCount(0)
                .build();
        resetTokenRepository.save(tokenEntity);

        String correlationId = UUID.randomUUID().toString();
        Map<String, Object> payloadMap = Map.of(
                "correlationId", correlationId,
                "userId", user.getId(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "otp", otp,
                "channel", channel,
                "purpose", "PASSWORD_RESET"
        );

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize OTP event payload", e);
        }

        OutboxEvent event = OutboxEvent.builder()
                .aggregateType("PASSWORD_RESET")
                .aggregateId(tokenEntity.getId())
                .eventType("OTP_REQUESTED")
                .payload(payloadJson)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();
        outboxRepository.save(event);
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email"));

        PasswordResetToken tokenEntity = resetTokenRepository
                .findTopByUserIdAndIsUsedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new OtpValidationException("No active OTP request found"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpValidationException("OTP has expired. Please request a new one.");
        }
        if (tokenEntity.getAttemptCount() >= 5) {
            throw new OtpValidationException("Too many incorrect attempts. Please request a new OTP.");
        }
        if (!passwordEncoder.matches(req.otp(), tokenEntity.getOtpHash())) {
            tokenEntity.setAttemptCount(tokenEntity.getAttemptCount() + 1);
            resetTokenRepository.save(tokenEntity);
            throw new OtpValidationException("Incorrect OTP");
        }

        return jwtUtil.generateResetToken(user.getId(), tokenEntity.getId());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        Long[] ids = jwtUtil.parseResetToken(req.resetToken());
        Long userId = ids[0];
        Long tokenId = ids[1];

        PasswordResetToken tokenEntity = resetTokenRepository.findById(tokenId)
                .orElseThrow(() -> new OtpValidationException("Invalid or expired reset token"));

        if (tokenEntity.isUsed()) {
            throw new OtpValidationException("This reset token has already been used");
        }
        if (!tokenEntity.getUserId().equals(userId)) {
            throw new OtpValidationException("Invalid reset token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        tokenEntity.setUsed(true);
        resetTokenRepository.save(tokenEntity);
    }
}