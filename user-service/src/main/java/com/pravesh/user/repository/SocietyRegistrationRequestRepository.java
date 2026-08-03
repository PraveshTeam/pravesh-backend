package com.pravesh.user.repository;

import com.pravesh.user.entity.SocietyRegistrationRequest;
import com.pravesh.user.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocietyRegistrationRequestRepository extends JpaRepository<SocietyRegistrationRequest, Long> {
    List<SocietyRegistrationRequest> findByStatus(RequestStatus status);
    Optional<SocietyRegistrationRequest> findTopByAdminUserIdOrderByCreatedAtDesc(Long adminUserId);
    boolean existsByAdminUserIdAndStatus(Long adminUserId, RequestStatus status);
}