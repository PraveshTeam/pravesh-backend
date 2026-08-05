package com.pravesh.user.repository;

import com.pravesh.user.entity.ResidentRelocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResidentRelocationHistoryRepository extends JpaRepository<ResidentRelocationHistory, Long> {
    List<ResidentRelocationHistory> findByResidentUserIdOrderByRelocatedAtDesc(Long residentUserId);
}