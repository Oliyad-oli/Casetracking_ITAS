package com.act.casemanagement.domain.valueobject;

/**
 * Represents a step in the approval chain. NONE means no approval pending.
 * SENIOR_MANAGER is deliberately its own enum constant — never alias with MANAGER.
 */
public enum ApprovalLevel {
    NONE,
    SUPERVISOR,
    MANAGER,
    SENIOR_MANAGER,
    COMMISSIONER;

    public CaseStatus toPendingStatus() {
        return switch (this) {
            case SUPERVISOR      -> CaseStatus.PENDING_SUPERVISOR_APPROVAL;
            case MANAGER         -> CaseStatus.PENDING_MANAGER_APPROVAL;
            case SENIOR_MANAGER  -> CaseStatus.PENDING_SENIOR_MANAGER_APPROVAL;
            case COMMISSIONER    -> CaseStatus.PENDING_COMMISSIONER_APPROVAL;
            case NONE            -> CaseStatus.APPROVED;
        };
    }
}
