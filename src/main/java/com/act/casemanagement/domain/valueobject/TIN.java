package com.act.casemanagement.domain.valueobject;

import com.act.casemanagement.domain.exception.DomainException;

/**
 * Taxpayer Identification Number — immutable, validated on construction.
 * Must be 10 digits per ITAS TIN format.
 */
public record TIN(String value) {

    public TIN {
        if (value == null || value.isBlank()) {
            throw new DomainException("TIN must not be blank");
        }
        String trimmed = value.strip();
        if (!trimmed.matches("\\d{10}")) {
            throw new DomainException("TIN must be exactly 10 digits, got: " + trimmed);
        }
        value = trimmed;
    }

    @Override
    public String toString() { return value; }
}
