package com.act.casemanagement.domain.event;

import com.act.casemanagement.domain.valueobject.AssociationType;

import java.time.Instant;
import java.util.UUID;

public record CasesAssociated(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,       // source caseId
    UUID targetCaseId,
    AssociationType relationshipType,
    UUID createdById
) implements DomainEvent {}
