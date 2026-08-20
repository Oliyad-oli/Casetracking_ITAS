package com.act.casemanagement.domain.valueobject;

import com.act.casemanagement.domain.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ETB-denominated monetary amount. Always 2 decimal places.
 * Currency is fixed to ETB — the ITAS platform is single-currency.
 * A negative amount is rejected; zero is allowed (e.g. empty case estimate).
 */
public record MonetaryAmount(BigDecimal value) {

    public static final String CURRENCY = "ETB";

    public MonetaryAmount {
        if (value == null) {
            throw new DomainException("MonetaryAmount value must not be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("MonetaryAmount must not be negative, got: " + value);
        }
        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static MonetaryAmount of(BigDecimal v) { return new MonetaryAmount(v); }
    public static MonetaryAmount of(double v)     { return new MonetaryAmount(BigDecimal.valueOf(v)); }
    public static MonetaryAmount zero()           { return new MonetaryAmount(BigDecimal.ZERO); }

    public MonetaryAmount add(MonetaryAmount other) {
        return new MonetaryAmount(this.value.add(other.value));
    }

    public boolean isGreaterThan(MonetaryAmount other) {
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isGreaterThanOrEqualTo(MonetaryAmount other) {
        return this.value.compareTo(other.value) >= 0;
    }

    public boolean isLessThanOrEqualTo(MonetaryAmount other) {
        return this.value.compareTo(other.value) <= 0;
    }

    @Override
    public String toString() { return value + " " + CURRENCY; }
}
