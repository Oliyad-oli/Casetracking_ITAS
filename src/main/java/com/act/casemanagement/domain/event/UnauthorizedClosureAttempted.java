package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Recorded when an actor tries to close a case they are not authorised to close.
 * This event must always produce an AUDIT log entry (CTR0700).
 */
public record UnauthorizedClosureAttempted(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,   // caseId
    UUID attemptingActorId,
    String attemptingActorRole,
    UUID assigningOfficerId
) implements DomainEvent {}
