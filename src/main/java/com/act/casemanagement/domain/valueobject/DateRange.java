package com.act.casemanagement.domain.valueobject;

import com.act.casemanagement.domain.exception.DomainException;

import java.time.LocalDate;

/** An inclusive date range. Start must not be after end. */
public record DateRange(LocalDate start, LocalDate end) {

    public DateRange {
        if (start == null || end == null) {
            throw new DomainException("DateRange start and end must not be null");
        }
        if (start.isAfter(end)) {
            throw new DomainException("DateRange start (" + start + ") must not be after end (" + end + ")");
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
}
