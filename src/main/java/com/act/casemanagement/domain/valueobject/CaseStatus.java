package com.act.casemanagement.domain.valueobject;

/**
 * All possible statuses a Case can hold.
 * PENDING_SENIOR_MANAGER_APPROVAL is deliberately distinct from PENDING_MANAGER_APPROVAL —
 * they are different approval levels with different actors (CTR0800 requirement).
 */
public enum CaseStatus {
    DRAFT,
    OPEN,
    UNASSIGNED,
    ASSIGNED,
    PLANNING,
    FIELD_WORK,
    UNDER_REVIEW,
    IN_PROGRESS,
    EVIDENCE_REVIEW,
    DRAFT_FINDINGS,
    PENDING_INFO,
    DECISION_PREPARED,
    PENDING_SUPERVISOR_APPROVAL,
    PENDING_MANAGER_APPROVAL,
    PENDING_SENIOR_MANAGER_APPROVAL,   // Deliberately distinct — never collapse with MANAGER
    PENDING_COMMISSIONER_APPROVAL,
    APPROVED,
    REJECTED,
    ESCALATED,
    READY_FOR_CLOSURE,
    CLOSED;

    public boolean isTerminal() {
        return this == CLOSED;
    }

    public boolean isPendingApproval() {
        return this == PENDING_SUPERVISOR_APPROVAL
            || this == PENDING_MANAGER_APPROVAL
            || this == PENDING_SENIOR_MANAGER_APPROVAL
            || this == PENDING_COMMISSIONER_APPROVAL;
    }
}
