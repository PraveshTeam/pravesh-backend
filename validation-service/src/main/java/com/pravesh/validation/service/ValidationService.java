package com.pravesh.validation.service;

import com.pravesh.validation.entity.EntryLog;
import com.pravesh.validation.entity.enums.ScanResult;
import com.pravesh.validation.exception.ShiftRequiredException;
import com.pravesh.validation.feign.*;
import com.pravesh.validation.repository.EntryLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

    private final PassFeignClient passFeignClient;
    private final UserFeignClient userFeignClient;
    private final NotificationFeignClient notificationFeignClient;
    private final EntryLogRepository entryLogRepository;

    public PassValidationResponse scan(String uuid, Long guardId, Long gateId, Long societyId) {
        ApiResponseWrapper<ShiftStatusResponse> shiftWrapper =
                userFeignClient.getShiftStatus(guardId);
        ShiftStatusResponse shiftStatus = shiftWrapper.data();

        if (!shiftStatus.hasActiveShift()) {
            throw new ShiftRequiredException(
                    "You must check in for your shift before scanning");
        }

        ApiResponseWrapper<PassValidationResponse> wrapper =
                passFeignClient.validateAndConsume(uuid, societyId);
        PassValidationResponse result = wrapper.data();

        EntryLog entryLog = EntryLog.builder()
                .passId(result.passId())
                .residentId(result.residentId())
                .visitorName(result.visitorName())
                .guardId(guardId)
                .gateId(gateId)
                .societyId(societyId)
                .shiftId(shiftStatus.shiftId())
                .scanResult(result.granted() ? ScanResult.GRANTED : ScanResult.DENIED)
                .denyReason(result.granted() ? null : result.reason())
                .build();

        entryLogRepository.save(entryLog);

        if (result.granted()) {
            try {
                notificationFeignClient.notifyVisitorEntered(new VisitorEnteredRequest(
                        result.residentId(), result.visitorName(),
                        "Gate " + gateId, LocalDateTime.now().toString()));
            } catch (Exception e) {
                log.warn("Failed to notify Notification-Service of visitor entry for pass {}: {}",
                        result.passId(), e.getMessage());
            }
        }

        return result;
    }

    public List<EntryLog> getEntriesByGate(Long gateId, java.time.LocalDate date, Long societyId) {
        java.time.LocalDateTime start = date.atStartOfDay();
        java.time.LocalDateTime end = date.plusDays(1).atStartOfDay();
        return entryLogRepository.findByGateIdAndScannedAtBetweenAndSocietyId(gateId, start, end, societyId);
    }

    public List<EntryLog> getEntriesByFlat(Long residentId, Long societyId) {
        return entryLogRepository.findByResidentIdAndSocietyId(residentId, societyId);
    }

    public List<EntryLog> getAllEntriesInSociety(Long societyId) {
        return entryLogRepository.findBySocietyId(societyId);
    }
}