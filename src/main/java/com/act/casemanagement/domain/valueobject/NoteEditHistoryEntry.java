package com.act.casemanagement.domain.valueobject;

import java.time.Instant;
import java.util.UUID;

/**
 * One entry in a CaseNote's immutable edit history.
 * previousContent and newContent carry the actual text (not just a message) —
 * required by CTR0500 note-integrity audit trail.
 */
public record NoteEditHistoryEntry(
    String previousContent,
    String newContent,
    Instant editedAt,
    UUID editedById,
    String editedByName,
    String reason
) {}
