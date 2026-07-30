package com.pravesh.user.repository;

import com.pravesh.user.entity.GuardShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardShiftRepository extends JpaRepository<GuardShift, Long> {
    Optional<GuardShift> findTopByGuardUserIdAndShiftEndIsNullOrderByShiftStartDesc(Long guardUserId);
    List<GuardShift> findByGuardUserIdOrderByShiftStartDesc(Long guardUserId);
}