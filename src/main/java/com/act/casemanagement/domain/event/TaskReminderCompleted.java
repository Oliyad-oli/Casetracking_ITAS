package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record TaskReminderCompleted(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,
    UUID taskId,
    UUID completedById
) implements DomainEvent {}
