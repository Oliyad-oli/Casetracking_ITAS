package com.act.casemanagement.domain.aggregate;

import com.act.casemanagement.domain.exception.DomainException;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Outbox entry — at-least-once delivery for cross-service side effects.
 * The dispatcher drains PENDING entries with exponential backoff.
 */
@Getter
public class OutboxEntry extends AggregateRoot {

    public enum Status { PENDING, SENT, FAILED, RETRY }

    private final UUID id;
    private final String eventType;
    private final Map<String, Object> payload;
    private final UUID aggregateId;
    private final String aggregateType;
    private Status status;
    private int attempts;
    private final int maxAttempts;
    private String lastError;
    private final Instant createdAt;
    private Instant nextAttemptAt;
    private Instant sentAt;

    public static OutboxEntry create(UUID id, String eventType, Map<String, Object> payload,
            UUID aggregateId, String aggregateType, int maxAttempts) {
        return new OutboxEntry(id, eventType, payload, aggregateId, aggregateType,
                Status.PENDING, 0, maxAttempts, null, Instant.now(), Instant.now(), null);
    }

    public static OutboxEntry reconstitute(UUID id, String eventType,
            Map<String, Object> payload, UUID aggregateId, String aggregateType,
            Status status, int attempts, int maxAttempts, String lastError,
            Instant createdAt, Instant nextAttemptAt, Instant sentAt) {
        return new OutboxEntry(id, eventType, payload, aggregateId, aggregateType,
                status, attempts, maxAttempts, lastError, createdAt, nextAttemptAt, sentAt);
    }

    private OutboxEntry(UUID id, String eventType, Map<String, Object> payload,
            UUID aggregateId, String aggregateType, Status status,
            int attempts, int maxAttempts, String lastError,
            Instant createdAt, Instant nextAttemptAt, Instant sentAt) {
        this.id = id; this.eventType = eventType; this.payload = payload;
        this.aggregateId = aggregateId; this.aggregateType = aggregateType;
        this.status = status; this.attempts = attempts; this.maxAttempts = maxAttempts;
        this.lastError = lastError; this.createdAt = createdAt;
        this.nextAttemptAt = nextAttemptAt; this.sentAt = sentAt;
    }

    public void markSent() {
        this.status = Status.SENT;
        this.sentAt = Instant.now();
    }

    public void recordFailure(String error, long backoffSeconds) {
        this.attempts++;
        this.lastError = error;
        if (this.attempts >= this.maxAttempts) {
            this.status = Status.FAILED;
        } else {
            this.status = Status.RETRY;
            this.nextAttemptAt = Instant.now().plusSeconds(backoffSeconds);
        }
    }

    public boolean isDispatchable() {
        return (status == Status.PENDING || status == Status.RETRY)
            && !nextAttemptAt.isAfter(Instant.now());
    }

    @Override public UUID getId() { return id; }
}
