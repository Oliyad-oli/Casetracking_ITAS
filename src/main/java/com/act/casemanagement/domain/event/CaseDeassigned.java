package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CaseDeassigned(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,
    UUID previousOfficerId,
    String previousOfficerName,
    UUID actorId,
    String reason
) implements DomainEvent {}
