package com.act.casemanagement.persistence.jpa.entity;

import com.act.casemanagement.persistence.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "cases")
@Getter @Setter
public class CaseEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "case_number", nullable = false, unique = true)
    private String caseNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false, length = 80)
    private String status;

    @Column(nullable = false, length = 20)
    private String priority;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(nullable = false, length = 30)
    private String tin;

    @Column(name = "taxpayer_name", nullable = false, length = 300)
    private String taxpayerName;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column(name = "created_by_name", length = 200)
    private String createdByName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "assigning_officer_id")
    private UUID assigningOfficerId;

    @Column(name = "assigning_officer_name", length = 200)
    private String assigningOfficerName;

    @Column(name = "assigned_officer_id")
    private UUID assignedOfficerId;

    @Column(name = "assigned_officer_name", length = 200)
    private String assignedOfficerName;

    @Column(name = "assigned_unit_id", length = 60)
    private String assignedUnitId;

    @Column(name = "assigned_unit_name", length = 200)
    private String assignedUnitName;

    @Column(name = "assigned_date")
    private Instant assignedDate;

    @Column(name = "assignment_reason", columnDefinition = "TEXT")
    private String assignmentReason;

    @Column(name = "is_self_assigned", nullable = false)
    private boolean selfAssigned;

    private Instant deadline;

    @Column(name = "target_days", nullable = false)
    private int targetDays;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;

    @Column(columnDefinition = "TEXT")
    private String reasons;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "estimated_amount_etb", nullable = false, precision = 20, scale = 2)
    private BigDecimal estimatedAmountEtb;

    @Column(name = "assessed_amount_etb", nullable = false, precision = 20, scale = 2)
    private BigDecimal assessedAmountEtb;

    @Column(name = "collected_amount_etb", nullable = false, precision = 20, scale = 2)
    private BigDecimal collectedAmountEtb;

    @Column(name = "written_off_amount_etb", nullable = false, precision = 20, scale = 2)
    private BigDecimal writtenOffAmountEtb;

    @Column(name = "refund_amount_etb", nullable = false, precision = 20, scale = 2)
    private BigDecimal refundAmountEtb;

    @Convert(converter = JsonbConverter.class)
    @Column(name = "type_specific_data", columnDefinition = "JSONB")
    private Map<String, Object> typeSpecificData;

    @Column(name = "current_approval_level", length = 40)
    private String currentApprovalLevel;

    // Closure fields
    @Column(name = "closure_reason", length = 60)
    private String closureReason;

    @Column(name = "closure_outcome", length = 60)
    private String closureOutcome;

    @Column(name = "closure_summary", columnDefinition = "TEXT")
    private String closureSummary;

    @Column(name = "closure_amount_assessed", precision = 20, scale = 2)
    private BigDecimal closureAmountAssessed;

    @Column(name = "closure_amount_collected", precision = 20, scale = 2)
    private BigDecimal closureAmountCollected;

    @Column(name = "closure_amount_written_off", precision = 20, scale = 2)
    private BigDecimal closureAmountWrittenOff;

    @Column(name = "closure_refund_approved", precision = 20, scale = 2)
    private BigDecimal closureRefundApproved;

    @Column(name = "closed_by_id")
    private UUID closedById;

    @Column(name = "closed_by_name", length = 200)
    private String closedByName;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closure_doc_id", length = 100)
    private String closureDocId;

    @Column(name = "is_draft", nullable = false)
    private boolean draft;
}
