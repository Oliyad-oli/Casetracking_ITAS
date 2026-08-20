package com.act.casemanagement.domain.aggregate;

import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class CaseTypeConfig extends AggregateRoot {

    private final UUID id;
    private Long version;
    private final CaseCategory code;
    private String name;
    private String description;
    private boolean active;
    private LocalDate effectiveDate;
    private String configVersion;
    private int defaultTargetDays;
    private List<Map<String, Object>> fieldsDefinition;
    private List<CaseStatus> allowedStatuses;
    private List<ApprovalLevel> approvalLevels;
    private final Instant createdAt;
    private Instant updatedAt;

    public static CaseTypeConfig create(UUID id, CaseCategory code, String name,
            String description, LocalDate effectiveDate, String configVersion,
            int defaultTargetDays, List<Map<String, Object>> fieldsDefinition,
            List<CaseStatus> allowedStatuses, List<ApprovalLevel> approvalLevels) {
        if (defaultTargetDays <= 0) {
            throw new DomainException("defaultTargetDays must be positive");
        }
        CaseTypeConfig c = new CaseTypeConfig(id, code, name, description, true,
                effectiveDate, configVersion, defaultTargetDays,
                fieldsDefinition, allowedStatuses, approvalLevels, Instant.now());
        c.updatedAt = c.createdAt;
        return c;
    }

    public static CaseTypeConfig reconstitute(UUID id, Long version, CaseCategory code,
            String name, String description, boolean active, LocalDate effectiveDate,
            String configVersion, int defaultTargetDays,
            List<Map<String, Object>> fieldsDefinition, List<CaseStatus> allowedStatuses,
            List<ApprovalLevel> approvalLevels, Instant createdAt, Instant updatedAt) {
        CaseTypeConfig c = new CaseTypeConfig(id, code, name, description, active,
                effectiveDate, configVersion, defaultTargetDays,
                fieldsDefinition, allowedStatuses, approvalLevels, createdAt);
        c.version   = version;
        c.updatedAt = updatedAt;
        return c;
    }

    private CaseTypeConfig(UUID id, CaseCategory code, String name, String description,
            boolean active, LocalDate effectiveDate, String configVersion,
            int defaultTargetDays, List<Map<String, Object>> fieldsDefinition,
            List<CaseStatus> allowedStatuses, List<ApprovalLevel> approvalLevels,
            Instant createdAt) {
        this.id = id; this.code = code; this.name = name; this.description = description;
        this.active = active; this.effectiveDate = effectiveDate;
        this.configVersion = configVersion; this.defaultTargetDays = defaultTargetDays;
        this.fieldsDefinition = fieldsDefinition; this.allowedStatuses = allowedStatuses;
        this.approvalLevels = approvalLevels; this.createdAt = createdAt;
    }

    @Override public UUID getId() { return id; }
    public void setVersion(Long v) { this.version = v; }
}
