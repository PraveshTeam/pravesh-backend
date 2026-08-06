package com.pravesh.validation.entity;

<<<<<<< Updated upstream
=======
import com.pravesh.validation.entity.enums.EntryType;
>>>>>>> Stashed changes
import com.pravesh.validation.entity.enums.ScanResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entry_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EntryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pass_id")
    private Long passId;

<<<<<<< Updated upstream
=======
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    @Builder.Default
    private EntryType entryType = EntryType.QR_PASS;

>>>>>>> Stashed changes
    @Column(name = "resident_id")
    private Long residentId;

    @Column(name = "visitor_name", length = 100)
    private String visitorName;

    @Column(name = "guard_id")
    private Long guardId;

    @Column(name = "gate_id")
    private Long gateId;

    @Column(name = "shift_id")
    private Long shiftId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_result", nullable = false, length = 10)
    private ScanResult scanResult;

    @Column(name = "deny_reason", length = 30)
    private String denyReason;

    @Column(name = "scanned_at", nullable = false, updatable = false)
    private LocalDateTime scannedAt;
    
    @Column(name = "society_id")
    private Long societyId;

    @PrePersist
    protected void onCreate() {
        this.scannedAt = LocalDateTime.now();
    }
}