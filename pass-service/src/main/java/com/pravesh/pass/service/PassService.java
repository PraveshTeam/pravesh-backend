package com.pravesh.pass.service;

import com.pravesh.pass.dto.request.CreatePassRequest;
import com.pravesh.pass.dto.response.PassLockResponse;
import com.pravesh.pass.dto.response.PassResponse;
import com.pravesh.pass.dto.response.PassValidationResponse;
import com.pravesh.pass.entity.VisitorPass;
import com.pravesh.pass.entity.enums.PassStatus;
import com.pravesh.pass.entity.enums.PassType;
import com.pravesh.pass.exception.InvalidStateException;
import com.pravesh.pass.exception.ResourceNotFoundException;
import com.pravesh.pass.feign.NotificationFeignClient;
import com.pravesh.pass.feign.PassCreatedRequest;
import com.pravesh.pass.feign.PassRevokedRequest;
import com.pravesh.pass.feign.UserFeignClient;
import com.pravesh.pass.repository.VisitorPassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassService {

	private final VisitorPassRepository passRepository;
	private final QRCodeService qrCodeService;
	private final NotificationFeignClient notificationFeignClient;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PassService.class);
	private final UserFeignClient userFeignClient;

	@Transactional
	public PassResponse createPass(Long residentId, Long societyId, CreatePassRequest req) {
		if (req.validFrom().isBefore(LocalDateTime.now())) {
			throw new InvalidStateException("validFrom cannot be in the past");
		}
		if (!req.validFrom().isBefore(req.validUntil())) {
			throw new InvalidStateException("validFrom must be before validUntil");
		}
		if (req.validUntil().isBefore(LocalDateTime.now())) {
			throw new InvalidStateException("validUntil must be in the future");
		}
		if (req.passType() == PassType.MULTI_USE && (req.usesAllowed() == null || req.usesAllowed() < 1)) {
			throw new InvalidStateException("usesAllowed is required for MULTI_USE passes");
		}

		String uuid = UUID.randomUUID().toString();

		VisitorPass pass = VisitorPass.builder().residentId(residentId).societyId(societyId).uuid(uuid)
				.visitorName(req.visitorName()).visitorPhone(req.visitorPhone()).passType(req.passType())
				.usesAllowed(req.passType() == PassType.MULTI_USE ? req.usesAllowed() : null)
				.usesRemaining(req.passType() == PassType.MULTI_USE ? req.usesAllowed() : null)
				.validFrom(req.validFrom()).validUntil(req.validUntil()).status(PassStatus.ACTIVE).build();

		pass = passRepository.save(pass);

		String qrBase64 = qrCodeService.generate(uuid);

		String flatNumber = "N/A";
		try {
			flatNumber = userFeignClient.getFlatNumber(residentId).data();
		} catch (Exception e) {
			log.warn("Failed to fetch flat number for resident {}: {}", residentId, e.getMessage());
		}

		try {
			notificationFeignClient.notifyPassCreated(new PassCreatedRequest(residentId, flatNumber, req.visitorName(),
					uuid, req.validFrom().toString(), req.validUntil().toString(), qrBase64));
		} catch (Exception e) {
			log.warn("Failed to notify Notification-Service of pass creation for resident {}: {}", residentId,
					e.getMessage());
		}

		return toResponse(pass, qrBase64);
	}

	public List<PassResponse> getMyActivePasses(Long residentId, Long societyId) {
		return passRepository.findByResidentIdAndStatusAndSocietyId(residentId, PassStatus.ACTIVE, societyId).stream()
				.map(p -> toResponse(p, null)).toList();
	}

	public List<PassResponse> getMyPassHistory(Long residentId, Long societyId) {
		return passRepository.findByResidentIdAndSocietyId(residentId, societyId).stream().map(p -> toResponse(p, null))
				.toList();
	}

	public PassResponse getPassDetail(Long passId, Long residentId) {
		VisitorPass pass = passRepository.findById(passId)
				.orElseThrow(() -> new ResourceNotFoundException("Pass not found"));
		if (!pass.getResidentId().equals(residentId)) {
			throw new org.springframework.security.access.AccessDeniedException("This pass does not belong to you");
		}
		return toResponse(pass, null);
	}

	public String regenerateQr(Long passId, Long residentId) {
		VisitorPass pass = passRepository.findById(passId)
				.orElseThrow(() -> new ResourceNotFoundException("Pass not found"));
		if (!pass.getResidentId().equals(residentId)) {
			throw new org.springframework.security.access.AccessDeniedException("This pass does not belong to you");
		}
		if (pass.getStatus() != PassStatus.ACTIVE) {
			throw new InvalidStateException("Only ACTIVE passes have a valid QR code");
		}
		return qrCodeService.generate(pass.getUuid());
	}

	private PassResponse toResponse(VisitorPass pass, String qrBase64) {
		return new PassResponse(pass.getId(), pass.getUuid(), pass.getVisitorName(), pass.getVisitorPhone(),
				pass.getPassType().name(), pass.getUsesAllowed(), pass.getUsesRemaining(), pass.getValidFrom(),
				pass.getValidUntil(), pass.getStatus().name(), qrBase64);
	}

	@Transactional
	public void revokePass(Long passId, Long residentId) {
		VisitorPass pass = passRepository.findById(passId)
				.orElseThrow(() -> new ResourceNotFoundException("Pass not found"));

		if (!pass.getResidentId().equals(residentId)) {
			throw new org.springframework.security.access.AccessDeniedException("This pass does not belong to you");
		}
		if (pass.getStatus() != PassStatus.ACTIVE) {
			throw new InvalidStateException(
					"Only ACTIVE passes can be revoked (current status: " + pass.getStatus() + ")");
		}

		pass.setStatus(PassStatus.REVOKED);
		passRepository.save(pass);

		try {
			notificationFeignClient.notifyPassRevoked(
					new PassRevokedRequest(pass.getResidentId(), pass.getVisitorName(), pass.getUuid()));
		} catch (Exception e) {
			log.warn("Failed to notify Notification-Service of pass revocation for pass {}: {}", pass.getId(),
					e.getMessage());
			// Non-fatal: revocation itself already succeeded and committed.
			// Notification-Service isn't live until Day 10 — this is expected until then.
		}
	}

	@Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
	public PassValidationResponse validateAndConsume(String uuid, Long callerSocietyId) {
		VisitorPass pass = passRepository.findByUuidForUpdate(uuid).orElse(null);

		if (pass == null) {
			return PassValidationResponse.denied("QR_INVALID");
		}

		if (!pass.getSocietyId().equals(callerSocietyId)) {
			return PassValidationResponse.denied("WRONG_SOCIETY");
		}

		LocalDateTime now = LocalDateTime.now();

		if (now.isBefore(pass.getValidFrom())) {
			return PassValidationResponse.denied("QR_NOT_YET_ACTIVE");
		}
		if (now.isAfter(pass.getValidUntil()) || pass.getStatus() == PassStatus.EXPIRED) {
			return PassValidationResponse.denied("QR_EXPIRED");
		}
		if (pass.getStatus() == PassStatus.REVOKED) {
			return PassValidationResponse.denied("REVOKED");
		}
		if (pass.getStatus() == PassStatus.CONSUMED) {
			return PassValidationResponse.denied("ALREADY_USED");
		}

		if (pass.getPassType() == PassType.ONE_TIME || pass.getPassType() == PassType.RECURRING_DAILY) {
			pass.setStatus(PassStatus.CONSUMED);
		} else if (pass.getPassType() == PassType.MULTI_USE) {
			pass.setUsesRemaining(pass.getUsesRemaining() - 1);
			if (pass.getUsesRemaining() <= 0) {
				pass.setStatus(PassStatus.CONSUMED);
			}
		}
		passRepository.save(pass);

		return PassValidationResponse.granted(pass.getId(), pass.getResidentId(), pass.getVisitorName(),
				pass.getPassType().name());
	}

	public List<PassResponse> getAllPassesInSociety(Long societyId) {
		return passRepository.findBySocietyId(societyId).stream().map(p -> toResponse(p, null)).toList();
	}
}