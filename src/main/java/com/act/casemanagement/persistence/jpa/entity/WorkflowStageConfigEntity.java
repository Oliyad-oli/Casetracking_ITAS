package com.act.casemanagement.persistence.jpa.entity;

import com.act.casemanagement.persistence.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_stage_configs")
@Getter @Setter
public class WorkflowStageConfigEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 60)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 80)
    private String status;

    @Convert(converter = StringListConverter.class)
    @Column(name = "allowed_roles", nullable = false, columnDefinition = "JSONB")
    private List<String> allowedRoles;

    @Convert(converter = StringListConverter.class)
    @Column(name = "allowed_actions", columnDefinition = "JSONB")
    private List<String> allowedActions;

    @Column(name = "sla_hours", nullable = false)
    private int slaHours;

    @Column(name = "next_status", length = 80)
    private String nextStatus;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
