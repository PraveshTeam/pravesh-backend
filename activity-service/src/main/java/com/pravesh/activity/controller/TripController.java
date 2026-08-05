package com.pravesh.activity.controller;

import com.pravesh.activity.dto.request.AddCommentRequest;
import com.pravesh.activity.dto.request.ProposeTripRequest;
import com.pravesh.activity.dto.request.RequestDecisionRequest;
import com.pravesh.activity.dto.response.ApiResponse;
import com.pravesh.activity.dto.response.CommentResponse;
import com.pravesh.activity.dto.response.JoinRequestResponse;
import com.pravesh.activity.dto.response.TripResponse;
import com.pravesh.activity.security.AuthenticatedUser;
import com.pravesh.activity.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping
    public ApiResponse<List<TripResponse>> listTrips(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Trips", tripService.listTrips(caller.societyId()));
    }

    @PostMapping
    public ApiResponse<TripResponse> proposeTrip(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody ProposeTripRequest req) {
        return ApiResponse.ok("Trip proposed", tripService.proposeTrip(req, caller.userId(), caller.societyId()));
    }

    @PostMapping("/{id}/join")
    public ApiResponse<JoinRequestResponse> requestToJoin(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Join request sent", tripService.requestToJoin(id, caller.userId(), caller.societyId()));
    }

    @GetMapping("/{id}/requests")
    public ApiResponse<List<JoinRequestResponse>> listRequests(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Join requests", tripService.listRequests(id, caller.userId(), caller.societyId()));
    }

    @PutMapping("/{id}/requests/{reqId}")
    public ApiResponse<JoinRequestResponse> decideRequest(
            @PathVariable Long id,
            @PathVariable Long reqId,
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody RequestDecisionRequest req) {
        return ApiResponse.ok("Request updated",
                tripService.decideRequest(id, reqId, req, caller.userId(), caller.societyId()));
    }

    @GetMapping("/{id}/participants")
    public ApiResponse<List<com.pravesh.activity.dto.response.ParticipantResponse>> getParticipants(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Participants", tripService.getParticipants(id, caller.userId(), caller.societyId()));
    }

    @GetMapping("/{id}/discussion")
    public ApiResponse<List<CommentResponse>> getDiscussion(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Discussion", tripService.getDiscussion(id, caller.userId(), caller.societyId()));
    }

    @PostMapping("/{id}/discussion")
    public ApiResponse<CommentResponse> addComment(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody AddCommentRequest req) {
        return ApiResponse.ok("Comment added", tripService.addComment(id, req, caller.userId(), caller.societyId()));
    }
}
