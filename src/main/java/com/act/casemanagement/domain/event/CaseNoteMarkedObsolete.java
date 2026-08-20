package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CaseNoteMarkedObsolete(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,
    UUID noteId,
    UUID actorId,
    String reason
) implements DomainEvent {}
