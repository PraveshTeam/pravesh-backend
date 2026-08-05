package com.pravesh.sos.repository;

import com.pravesh.sos.entity.SosStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SosStatusHistoryRepository extends JpaRepository<SosStatusHistory, Long> {
    List<SosStatusHistory> findBySosAlertIdOrderByChangedAtAsc(Long sosAlertId);
}
