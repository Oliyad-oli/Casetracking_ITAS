package com.act.casemanagement.domain.event;

import com.act.casemanagement.domain.valueobject.CaseStatus;

import java.time.Instant;
import java.util.UUID;

public record CaseStatusChanged(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,
    CaseStatus previousStatus,
    CaseStatus newStatus,
    UUID actorId,
    String reason
) implements DomainEvent {}
