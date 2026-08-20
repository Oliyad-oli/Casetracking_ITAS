package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CaseAssigned(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,   // caseId
    UUID assignedOfficerId,
    String assignedOfficerName,
    UUID assigningActorId,
    String reason
) implements DomainEvent {}
