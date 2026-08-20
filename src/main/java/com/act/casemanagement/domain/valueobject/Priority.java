package com.act.casemanagement.domain.valueobject;

public enum Priority {
    CRITICAL, HIGH, MEDIUM, LOW;

    public boolean isHighOrAbove() {
        return this == CRITICAL || this == HIGH;
    }
}
