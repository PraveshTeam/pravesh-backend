package com.pravesh.user.controller;

import com.pravesh.user.dto.response.ApiResponse;
import com.pravesh.user.entity.Resident;
import com.pravesh.user.repository.ResidentRepository;
import com.pravesh.user.dto.response.FlatInternalResponse;
import com.pravesh.user.dto.response.ShiftStatusResponse;
import com.pravesh.user.entity.Flat;
import com.pravesh.user.entity.User;
import com.pravesh.user.exception.ResourceNotFoundException;
import com.pravesh.user.repository.FlatRepository;
import com.pravesh.user.repository.UserRepository;
import com.pravesh.user.service.ShiftCheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

	private final FlatRepository flatRepository;
	private final ShiftCheckinService shiftCheckinService;
	private final UserRepository userRepository;
	private final ResidentRepository residentRepository;

	@GetMapping("/flats/{id}")
	public ApiResponse<FlatInternalResponse> getFlat(@PathVariable Long id) {
		Flat flat = flatRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flat not found"));
		return ApiResponse.ok("Flat detail", new FlatInternalResponse(flat.getId(), flat.getSocietyId(),
				flat.getFlatNumber(), flat.getTower(), flat.getResidentId()));
	}

	@GetMapping("/guards/{guardUserId}/shift-status")
	public ApiResponse<ShiftStatusResponse> shiftStatus(@PathVariable Long guardUserId) {
		var activeShiftId = shiftCheckinService.getActiveShiftId(guardUserId);
		return ApiResponse.ok("Shift status",
				new ShiftStatusResponse(activeShiftId.isPresent(), activeShiftId.orElse(null)));
	}

	@GetMapping("/users/{id}/contact")
	public ApiResponse<com.pravesh.user.dto.response.UserContactResponse> getContact(@PathVariable Long id) {
		User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return ApiResponse.ok("User contact", new com.pravesh.user.dto.response.UserContactResponse(user.getId(),
				user.getName(), user.getEmail(), user.getPhone()));
	}

	@GetMapping("/residents/{id}/flat-number")
	public ApiResponse<String> getFlatNumber(@PathVariable Long id) {
		Resident resident = residentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

		if (resident.getFlatId() == null) {
			return ApiResponse.ok("Flat number", "Unassigned");
		}

		String flatNumber = flatRepository.findById(resident.getFlatId()).map(Flat::getFlatNumber).orElse("Unknown");

		return ApiResponse.ok("Flat number", flatNumber);
	}
}