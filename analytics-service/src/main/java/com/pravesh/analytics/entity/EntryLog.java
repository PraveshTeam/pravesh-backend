package com.pravesh.analytics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "entry_logs")
@Getter @Setter
public class EntryLog {

    @Id
    private Long id;

    @Column(name = "pass_id")
    private Long passId;

    @Column(name = "resident_id")
    private Long residentId;

    @Column(name = "visitor_name")
    private String visitorName;

    @Column(name = "guard_id")
    private Long guardId;

    @Column(name = "gate_id")
    private Long gateId;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "scan_result")
    private String scanResult;

    @Column(name = "deny_reason")
    private String denyReason;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;
}