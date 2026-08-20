package com.act.casemanagement.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_records")
@Getter @Setter
public class ApprovalRecordEntity {

    @Id
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(nullable = false, length = 40)
    private String level;

    @Column(name = "approver_id", nullable = false)
    private UUID approverId;

    @Column(name = "approver_name", length = 200)
    private String approverName;

    @Column(name = "approver_role", nullable = false, length = 60)
    private String approverRole;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "next_level", length = 40)
    private String nextLevel;
}
