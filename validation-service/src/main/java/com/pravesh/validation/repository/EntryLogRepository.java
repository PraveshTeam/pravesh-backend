package com.pravesh.validation.repository;

import com.pravesh.validation.entity.EntryLog;
import com.pravesh.validation.entity.enums.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EntryLogRepository extends JpaRepository<EntryLog, Long> {
    List<EntryLog> findByGateIdAndScannedAtBetween(
            Long gateId, LocalDateTime start, LocalDateTime end);

    List<EntryLog> findByScanResult(ScanResult scanResult);

    List<EntryLog> findByResidentId(Long residentId);

    long countByScannedAtBetween(LocalDateTime start, LocalDateTime end);
    
    
    List<EntryLog> findByGateIdAndScannedAtBetweenAndSocietyId(
            Long gateId, LocalDateTime start, LocalDateTime end, Long societyId);
    List<EntryLog> findByResidentIdAndSocietyId(Long residentId, Long societyId);
    List<EntryLog> findBySocietyId(Long societyId);
}