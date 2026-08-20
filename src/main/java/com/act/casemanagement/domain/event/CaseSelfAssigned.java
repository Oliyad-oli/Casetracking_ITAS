package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Distinct from CaseAssigned — emitted when the assigning actor and the
 * assigned officer are the same person (CTR0200/CTR0300 self-assignment rule).
 * The justification field is mandatory for self-assignments.
 */
public record CaseSelfAssigned(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,   // caseId
    UUID officerId,
    String officerName,
    String mandatoryJustification
) implements DomainEvent {}
