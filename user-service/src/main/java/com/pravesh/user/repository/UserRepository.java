package com.pravesh.user.repository;

import com.pravesh.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    
    List<User> findByRole(com.pravesh.user.entity.enums.Role role);
}