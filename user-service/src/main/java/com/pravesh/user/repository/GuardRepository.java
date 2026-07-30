package com.pravesh.user.repository;

import com.pravesh.user.entity.Guard;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardRepository extends JpaRepository<Guard, Long> {
    boolean existsByGateId(Long gateId);
}