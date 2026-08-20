package com.act.casemanagement.domain.aggregate;

import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import com.act.casemanagement.domain.valueobject.UserRole;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class WorkflowStageConfig extends AggregateRoot {

    private final UUID id;
    private Long version;
    private final String name;
    private final CaseCategory category;   // null = applies to all categories
    private final String description;
    private final CaseStatus status;
    private final List<UserRole> allowedRoles;
    private final List<String> allowedActions;
    private final int slaHours;
    private final CaseStatus nextStatus;   // null = no automatic next step
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    public static WorkflowStageConfig reconstitute(UUID id, Long version, String name,
            CaseCategory category, String description, CaseStatus status,
            List<UserRole> allowedRoles, List<String> allowedActions,
            int slaHours, CaseStatus nextStatus, boolean active,
            Instant createdAt, Instant updatedAt) {
        return new WorkflowStageConfig(id, version, name, category, description,
                status, allowedRoles, allowedActions, slaHours, nextStatus,
                active, createdAt, updatedAt);
    }

    private WorkflowStageConfig(UUID id, Long version, String name, CaseCategory category,
            String description, CaseStatus status, List<UserRole> allowedRoles,
            List<String> allowedActions, int slaHours, CaseStatus nextStatus,
            boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id; this.version = version; this.name = name; this.category = category;
        this.description = description; this.status = status; this.allowedRoles = allowedRoles;
        this.allowedActions = allowedActions; this.slaHours = slaHours;
        this.nextStatus = nextStatus; this.active = active;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    @Override public UUID getId() { return id; }
    public void setVersion(Long v) { this.version = v; }
}
