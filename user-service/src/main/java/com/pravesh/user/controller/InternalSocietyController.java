package com.pravesh.user.controller;

import com.pravesh.user.dto.response.ApiResponse;
import com.pravesh.user.entity.*;
import com.pravesh.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class InternalSocietyController {

    private final GateRepository gateRepository;
    private final GuardShiftRepository guardShiftRepository;
    private final GuardRepository guardRepository;
    private final UserRepository userRepository;
    private final SocietyAdminRepository societyAdminRepository;

    @GetMapping("/api/internal/societies/{societyId}/emergency-contact")
    public ApiResponse<EmergencyContactResponse> getEmergencyContact(@PathVariable Long societyId) {
        List<Gate> gates = gateRepository.findBySocietyId(societyId);

        for (Gate gate : gates) {
            Optional<GuardShift> activeShift = guardShiftRepository
                    .findTopByGateIdAndShiftEndIsNullOrderByShiftStartDesc(gate.getId());
            if (activeShift.isPresent()) {
                Long guardUserId = activeShift.get().getGuardUserId();
                User guardUser = userRepository.findById(guardUserId).orElse(null);
                if (guardUser != null) {
                    return ApiResponse.ok("Emergency contact resolved",
                            new EmergencyContactResponse("GUARD", guardUser.getName(), guardUser.getPhone()));
                }
            }
        }

        // No guard currently on duty anywhere in this society — fall back to admin.
        EmergencyContactResponse fallback = societyAdminRepository.findBySocietyId(societyId).stream()
                .findFirst()
                .map(admin -> {
                    User adminUser = userRepository.findById(admin.getUserId()).orElse(null);
                    return adminUser != null
                            ? new EmergencyContactResponse("ADMIN", adminUser.getName(), adminUser.getPhone())
                            : new EmergencyContactResponse("NONE", null, null);
                })
                .orElse(new EmergencyContactResponse("NONE", null, null));

        return ApiResponse.ok("Emergency contact resolved", fallback);
    }

    public record EmergencyContactResponse(String role, String name, String phone) {}
}