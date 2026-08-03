package com.pravesh.user.repository;

import com.pravesh.user.entity.Society;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SocietyRepository extends JpaRepository<Society, Long> {
	
	boolean existsByNameAndCity(String name, String city);
	List<Society> findByNameContainingIgnoreCase(String name);
}

