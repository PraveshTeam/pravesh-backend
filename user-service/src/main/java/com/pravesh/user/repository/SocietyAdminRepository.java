package com.pravesh.user.repository;

import com.pravesh.user.entity.SocietyAdmin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SocietyAdminRepository extends JpaRepository<SocietyAdmin, Long> {
	
	List<SocietyAdmin> findBySocietyId(Long societyId);
}