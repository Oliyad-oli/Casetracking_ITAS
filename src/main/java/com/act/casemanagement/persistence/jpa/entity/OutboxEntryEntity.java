package com.act.casemanagement.persistence.jpa.entity;

import com.act.casemanagement.persistence.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "outbox_entries")
@Getter @Setter
public class OutboxEntryEntity {

    @Id
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Convert(converter = JsonbConverter.class)
    @Column(nullable = false, columnDefinition = "JSONB")
    private Map<String, Object> payload;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "sent_at")
    private Instant sentAt;
}
