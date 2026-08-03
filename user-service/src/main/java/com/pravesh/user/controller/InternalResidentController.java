package com.pravesh.user.controller;

import com.pravesh.user.entity.Flat;
import com.pravesh.user.entity.Resident;
import com.pravesh.user.entity.User;
import com.pravesh.user.exception.ResourceNotFoundException;
import com.pravesh.user.repository.FlatRepository;
import com.pravesh.user.repository.ResidentRepository;
import com.pravesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InternalResidentController {

    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;
    private final FlatRepository flatRepository;

    @GetMapping("/api/internal/residents/{userId}/context")
    public ResidentContextResponse getResidentContext(@PathVariable Long userId) {
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String flatNumber = null;
        Long societyId = null;
        if (resident.getFlatId() != null) {
            Flat flat = flatRepository.findById(resident.getFlatId()).orElse(null);
            if (flat != null) {
                flatNumber = flat.getFlatNumber();
                societyId = flat.getSocietyId();
            }
        }

        return new ResidentContextResponse(
                user.getId(), user.getName(), user.getPhone(),
                resident.getFlatId(), flatNumber, societyId);
    }

    public record ResidentContextResponse(
            Long userId, String name, String phone,
            Long flatId, String flatNumber, Long societyId) {}
}