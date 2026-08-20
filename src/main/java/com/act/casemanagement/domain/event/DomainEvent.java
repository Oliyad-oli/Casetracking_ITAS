package com.act.casemanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Marker interface for all domain events. */
public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
    UUID aggregateId();
}
