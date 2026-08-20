package com.act.casemanagement.domain.event;

import com.act.casemanagement.domain.valueobject.ApprovalAction;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import com.act.casemanagement.domain.valueobject.CaseStatus;

import java.time.Instant;
import java.util.UUID;

public record ApprovalProcessed(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,   // caseId
    UUID approverId,
    String approverName,
    ApprovalLevel level,
    ApprovalAction action,
    CaseStatus newStatus,
    ApprovalLevel nextLevel,
    String comments
) implements DomainEvent {}
