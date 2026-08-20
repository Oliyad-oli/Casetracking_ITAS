package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record TaskReminderCancelled(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,
    UUID taskId,
    UUID cancelledById,
    String reason
) implements DomainEvent {}
