package com.pravesh.activity.repository;

import com.pravesh.activity.entity.TripComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripCommentRepository extends JpaRepository<TripComment, Long> {
    List<TripComment> findByTripIdOrderByCreatedAtAsc(Long tripId);
}
