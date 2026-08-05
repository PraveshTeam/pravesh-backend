package com.pravesh.user.controller;

import com.pravesh.user.dto.response.ApiResponse;
import com.pravesh.user.dto.response.UserProfileResponse;
import com.pravesh.user.entity.Flat;
import com.pravesh.user.entity.Society;
import com.pravesh.user.entity.User;
import com.pravesh.user.entity.enums.Role;
import com.pravesh.user.exception.ResourceNotFoundException;
import com.pravesh.user.repository.FlatRepository;
import com.pravesh.user.repository.GuardRepository;
import com.pravesh.user.repository.ResidentRepository;
import com.pravesh.user.repository.SocietyAdminRepository;
import com.pravesh.user.repository.SocietyRepository;
import com.pravesh.user.repository.UserRepository;
import com.pravesh.user.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final GuardRepository guardRepository;
    private final SocietyAdminRepository societyAdminRepository;
    private final FlatRepository flatRepository;
    private final SocietyRepository societyRepository;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMe(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Profile", buildProfile(caller.userId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateMe(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestBody UpdateProfileRequest req) {

        User user = userRepository.findById(caller.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (req.name() != null && !req.name().isBlank()) {
            user.setName(req.name());
        }
        if (req.phone() != null && !req.phone().isBlank()) {
            user.setPhone(req.phone());
        }
        userRepository.save(user);

        return ApiResponse.ok("Profile updated", buildProfile(caller.userId()));
    }

    private UserProfileResponse buildProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String verificationStatus = null;
        Long flatId = null, gateId = null, societyId = null;

        if (user.getRole() == Role.RESIDENT) {
            var r = residentRepository.findById(user.getId()).orElse(null);
            if (r != null) {
                verificationStatus = r.getVerificationStatus().name();
                flatId = r.getFlatId();
            }
        } else if (user.getRole() == Role.GUARD) {
            var g = guardRepository.findById(user.getId()).orElse(null);
            if (g != null) gateId = g.getGateId();
        } else if (user.getRole() == Role.SOCIETY_ADMIN) {
            var a = societyAdminRepository.findById(user.getId()).orElse(null);
            if (a != null) {
                verificationStatus = a.getVerificationStatus().name();
                societyId = a.getSocietyId();
            }
        }

        String flatNumber = null, tower = null, societyName = null;

        if (flatId != null) {
            Flat flat = flatRepository.findById(flatId).orElse(null);
            if (flat != null) {
                flatNumber = flat.getFlatNumber();
                tower = flat.getTower();
                if (societyId == null) {
                    societyId = flat.getSocietyId();
                }
            }
        }
        if (societyId != null) {
            societyName = societyRepository.findById(societyId).map(Society::getName).orElse(null);
        }

        return new UserProfileResponse(
                user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRole().name(), user.getState(), user.isActive(),
                verificationStatus, flatId, gateId, societyId,
                flatNumber, tower, societyName);
    }

    public record UpdateProfileRequest(String name, String phone) {}
}