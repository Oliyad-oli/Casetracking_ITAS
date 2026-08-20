package com.act.casemanagement.domain.event;

import com.act.casemanagement.domain.valueobject.CaseCategory;

import java.time.Instant;
import java.util.UUID;

public record CaseCreated(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,   // caseId
    String caseNumber,
    CaseCategory category,
    String tin,
    UUID createdById
) implements DomainEvent {}
