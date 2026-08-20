package com.act.casemanagement.domain.event;

import com.act.casemanagement.domain.valueobject.CaseClosureDetails;

import java.time.Instant;
import java.util.UUID;

public record CaseClosed(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,
    boolean isDirectorateOverride,
    UUID closedById,
    CaseClosureDetails.ClosureReason closureReason
) implements DomainEvent {}
