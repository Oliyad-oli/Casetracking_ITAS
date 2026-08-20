package com.act.casemanagement.domain.valueobject;

/**
 * Mirrors the role set the gateway forwards via X-Authenticated-Role.
 * Used for report-scope decisions and approval-chain matching.
 */
public enum UserRole {
    TAX_OFFICER,
    SUPERVISOR,
    MANAGER,
    SENIOR_MANAGER,
    COMMISSIONER,
    SYSTEM_ADMINISTRATOR;

    public boolean isManagerOrAbove() {
        return this == MANAGER || this == SENIOR_MANAGER
            || this == COMMISSIONER || this == SYSTEM_ADMINISTRATOR;
    }

    public boolean isDirectorateOverride() {
        return this == MANAGER || this == SENIOR_MANAGER || this == COMMISSIONER;
    }
}
