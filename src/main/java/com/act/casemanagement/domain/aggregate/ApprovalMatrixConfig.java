package com.act.casemanagement.domain.aggregate;

import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.RiskLevel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Approval matrix rule — drives ApprovalEscalationService.
 * A null caseType means "ALL"; a null riskLevel means "ALL".
 */
@Getter
public class ApprovalMatrixConfig extends AggregateRoot {

    private final UUID id;
    private Long version;
    private final CaseCategory caseType;   // null = ALL
    private final BigDecimal minValueEtb;
    private final BigDecimal maxValueEtb;  // null = unbounded
    private final RiskLevel riskLevel;     // null = ALL
    private final List<ApprovalLevel> requiredApprovals;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    public static ApprovalMatrixConfig create(UUID id, CaseCategory caseType,
            BigDecimal minValueEtb, BigDecimal maxValueEtb, RiskLevel riskLevel,
            List<ApprovalLevel> requiredApprovals) {
        return new ApprovalMatrixConfig(id, null, caseType, minValueEtb, maxValueEtb,
                riskLevel, requiredApprovals, true, Instant.now(), Instant.now());
    }

    public static ApprovalMatrixConfig reconstitute(UUID id, Long version,
            CaseCategory caseType, BigDecimal minValueEtb, BigDecimal maxValueEtb,
            RiskLevel riskLevel, List<ApprovalLevel> requiredApprovals,
            boolean active, Instant createdAt, Instant updatedAt) {
        return new ApprovalMatrixConfig(id, version, caseType, minValueEtb, maxValueEtb,
                riskLevel, requiredApprovals, active, createdAt, updatedAt);
    }

    private ApprovalMatrixConfig(UUID id, Long version, CaseCategory caseType,
            BigDecimal minValueEtb, BigDecimal maxValueEtb, RiskLevel riskLevel,
            List<ApprovalLevel> requiredApprovals, boolean active,
            Instant createdAt, Instant updatedAt) {
        this.id = id; this.version = version; this.caseType = caseType;
        this.minValueEtb = minValueEtb; this.maxValueEtb = maxValueEtb;
        this.riskLevel = riskLevel; this.requiredApprovals = requiredApprovals;
        this.active = active; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    @Override public UUID getId() { return id; }
    public void setVersion(Long v) { this.version = v; }
}
