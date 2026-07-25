package com.pravesh.user.repository;

import com.pravesh.user.entity.Gate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GateRepository extends JpaRepository<Gate, Long> {
    List<Gate> findBySocietyId(Long societyId);
}