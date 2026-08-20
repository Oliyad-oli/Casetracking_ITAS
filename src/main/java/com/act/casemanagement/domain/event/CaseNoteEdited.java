package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Carries the actual before/after text for the audit trail (CTR0500).
 * A content preview is not sufficient here — the full previous and new
 * content are required to prove the edit after the fact.
 */
public record CaseNoteEdited(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,   // caseId
    UUID noteId,
    UUID editorId,
    String previousContent,
    String newContent,
    String reason
) implements DomainEvent {}
