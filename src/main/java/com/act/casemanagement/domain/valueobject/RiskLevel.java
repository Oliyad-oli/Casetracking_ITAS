package com.act.casemanagement.domain.valueobject;

/**
 * Risk level feeds into ApprovalEscalationService matching:
 * case category + risk level + ETB value range → required approval chain.
 */
public enum RiskLevel {
    CRITICAL, HIGH, MEDIUM, LOW;

    public boolean isHighOrAbove() {
        return this == CRITICAL || this == HIGH;
    }
}
