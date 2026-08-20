package com.act.casemanagement.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter @Setter
public class AuditLogEntity {

    @Id
    private UUID id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "actor_name", length = 200)
    private String actorName;

    @Column(name = "actor_role", nullable = false, length = 60)
    private String actorRole;

    @Column(nullable = false, length = 120)
    private String action;

    @Column(name = "case_id")
    private UUID caseId;

    @Column(name = "case_number", length = 60)
    private String caseNumber;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}
