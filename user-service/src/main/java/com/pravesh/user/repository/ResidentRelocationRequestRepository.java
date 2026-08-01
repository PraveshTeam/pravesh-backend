package com.pravesh.user.repository;

import com.pravesh.user.entity.enums.RequestStatus;
import com.pravesh.user.entity.ResidentRelocationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResidentRelocationRequestRepository extends JpaRepository<ResidentRelocationRequest, Long> {

    List<ResidentRelocationRequest> findByTargetSocietyIdAndStatusOrderByCreatedAtDesc(
            Long targetSocietyId, RequestStatus status);

    Optional<ResidentRelocationRequest> findByResidentUserIdAndStatus(
            Long residentUserId, RequestStatus status);
}