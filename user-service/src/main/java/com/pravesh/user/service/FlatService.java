package com.pravesh.user.service;

import com.pravesh.user.dto.response.FlatResponse;
import com.pravesh.user.repository.FlatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlatService {

    private final FlatRepository flatRepository;

    public List<FlatResponse> listFlats(Long societyId) {
        return flatRepository.findBySocietyId(societyId).stream()
                .map(f -> new FlatResponse(f.getId(), f.getFlatNumber(), f.getTower(), f.getResidentId()))
                .toList();
    }
}