package com.act.casemanagement.domain.model;

import com.act.casemanagement.domain.valueobject.ApprovalAction;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import com.act.casemanagement.domain.valueobject.UserRole;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** Immutable approval record — child of the Case aggregate. Append-only. */
@Getter
public class ApprovalRecord {

    private final UUID id;
    private final UUID caseId;
    private final ApprovalLevel level;
    private final UUID approverId;
    private final String approverName;
    private final UserRole approverRole;
    private final ApprovalAction action;
    private final String comments;
    private final Instant occurredAt;
    private final ApprovalLevel nextLevel;

    public static ApprovalRecord of(UUID id, UUID caseId, ApprovalLevel level,
            UUID approverId, String approverName, UserRole approverRole,
            ApprovalAction action, String comments, Instant occurredAt,
            ApprovalLevel nextLevel) {
        return new ApprovalRecord(id, caseId, level, approverId, approverName,
                approverRole, action, comments, occurredAt, nextLevel);
    }

    private ApprovalRecord(UUID id, UUID caseId, ApprovalLevel level,
            UUID approverId, String approverName, UserRole approverRole,
            ApprovalAction action, String comments, Instant occurredAt,
            ApprovalLevel nextLevel) {
        this.id           = id;
        this.caseId       = caseId;
        this.level        = level;
        this.approverId   = approverId;
        this.approverName = approverName;
        this.approverRole = approverRole;
        this.action       = action;
        this.comments     = comments;
        this.occurredAt   = occurredAt;
        this.nextLevel    = nextLevel;
    }
}
