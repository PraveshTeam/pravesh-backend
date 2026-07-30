package com.pravesh.user.service;

import com.pravesh.user.dto.response.OnboardingRequestResponse;
import com.pravesh.user.entity.*;
import com.pravesh.user.entity.enums.DocumentType;
import com.pravesh.user.entity.enums.RequestStatus;
import com.pravesh.user.entity.enums.VerificationStatus;
import com.pravesh.user.exception.*;
import com.pravesh.user.feign.NotificationFeignClient;
import com.pravesh.user.feign.ResidentApprovedRequest;
import com.pravesh.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);

    private final FlatAccessRequestRepository requestRepository;
    private final ResidentRepository residentRepository;
    private final FlatRepository flatRepository;
    private final UserRepository userRepository;
    private final DocumentStorageService documentStorageService;
    private final NotificationFeignClient notificationFeignClient;

    @Transactional
    public OnboardingRequestResponse submitRequest(
            Long userId, Long societyId, String claimedFlatNumber, String tower,
            DocumentType documentType, MultipartFile file) {

        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident record not found"));

        if (resident.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new InvalidStateException(
                    "Onboarding request not allowed — account is already " +
                            resident.getVerificationStatus());
        }
        if (requestRepository.existsByUserIdAndStatus(userId, RequestStatus.PENDING)) {
            throw new DuplicateResourceException(
                    "You already have a pending onboarding request");
        }

        String path = documentStorageService.store(userId, file);

        FlatAccessRequest request = FlatAccessRequest.builder()
                .userId(userId)
                .societyId(societyId)
                .claimedFlatNumber(claimedFlatNumber)
                .tower(tower)
                .documentType(documentType)
                .documentPath(path)
                .status(RequestStatus.PENDING)
                .build();

        request = requestRepository.save(request);
        return toResponse(request);
    }

    public OnboardingRequestResponse getMyLatestRequest(Long userId) {
        FlatAccessRequest request = requestRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No onboarding request found"));
        return toResponse(request);
    }

    public List<OnboardingRequestResponse> listByStatus(RequestStatus status, Long societyId) {
        return requestRepository.findByStatusAndSocietyId(status, societyId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Resource getDocumentForDownload(Long requestId, Long callerId, String callerRole, Long callerSocietyId) {
        FlatAccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));

        boolean isOwner = request.getUserId().equals(callerId);
        boolean isAdminOfThisSociety = "SOCIETY_ADMIN".equals(callerRole)
                && request.getSocietyId().equals(callerSocietyId);

        if (!isOwner && !isAdminOfThisSociety) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not permitted to access this document");
        }

        Path path = Path.of(request.getDocumentPath());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Document file not found on server");
        }
        return resource;
    }

    @Transactional
    public OnboardingRequestResponse approve(Long requestId, Long reviewerId, Long callerSocietyId) {
        FlatAccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));

        if (!request.getSocietyId().equals(callerSocietyId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not permitted to review requests for a different society");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been reviewed");
        }

        Flat flat = flatRepository
                .findBySocietyIdAndFlatNumber(request.getSocietyId(), request.getClaimedFlatNumber())
                .orElse(null);

        if (flat == null) {
            flat = Flat.builder()
                    .societyId(request.getSocietyId())
                    .flatNumber(request.getClaimedFlatNumber())
                    .tower(request.getTower())
                    .residentId(null)
                    .build();
            flat = flatRepository.save(flat);
        }

        if (flat.getResidentId() != null) {
            throw new DuplicateResourceException(
                    "Flat " + flat.getFlatNumber() + " already has a resident assigned");
        }

        Resident resident = residentRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Resident record not found"));

        resident.setFlatId(flat.getId());
        resident.setVerificationStatus(VerificationStatus.VERIFIED);
        resident.setMovedInDate(java.time.LocalDate.now());
        residentRepository.save(resident);

        flat.setResidentId(request.getUserId());
        flatRepository.save(flat);

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        request = requestRepository.save(request);

        try {
            notificationFeignClient.notifyResidentApproved(
                    new ResidentApprovedRequest(request.getUserId(), flat.getFlatNumber()));
        } catch (Exception e) {
            log.warn("Failed to notify resident {} of approval: {}",
                    request.getUserId(), e.getMessage());
        }

        return toResponse(request);
    }

    @Transactional
    public OnboardingRequestResponse reject(Long requestId, Long reviewerId, String reason, Long callerSocietyId) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidStateException("A rejection reason is required");
        }

        FlatAccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));

        if (!request.getSocietyId().equals(callerSocietyId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not permitted to review requests for a different society");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been reviewed");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setAdminNotes(reason);
        request.setReviewedBy(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        request = requestRepository.save(request);

        Resident resident = residentRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Resident record not found"));
        resident.setVerificationStatus(VerificationStatus.PENDING);
        residentRepository.save(resident);

        return toResponse(request);
    }

    private OnboardingRequestResponse toResponse(FlatAccessRequest request) {
        String userName = userRepository.findById(request.getUserId())
                .map(User::getName)
                .orElse("Unknown");

        return new OnboardingRequestResponse(
                request.getId(),
                request.getUserId(),
                userName,
                request.getClaimedFlatNumber(),
                request.getTower(),
                request.getDocumentType().name(),
                request.getStatus().name(),
                request.getAdminNotes(),
                request.getCreatedAt(),
                request.getReviewedAt()
        );
    }
}