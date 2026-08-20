package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskReminderCreated(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,   // caseId
    UUID taskId,
    UUID assignedToUserId,
    String title,
    LocalDate dueDate
) implements DomainEvent {}
