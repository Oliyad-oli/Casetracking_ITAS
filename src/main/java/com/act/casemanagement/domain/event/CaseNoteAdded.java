package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CaseNoteAdded(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,   // caseId
    UUID noteId,
    UUID authorId,
    String contentPreview   // first 80 chars for audit; full text in note entity
) implements DomainEvent {}
