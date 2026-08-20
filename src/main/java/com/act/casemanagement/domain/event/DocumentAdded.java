package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Triggers AutomaticTransitionService (DOC_UPLOAD trigger). */
public record DocumentAdded(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,   // caseId
    UUID documentId,
    UUID uploadedById,
    String documentName
) implements DomainEvent {}
