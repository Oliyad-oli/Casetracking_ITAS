package com.act.casemanagement.api.dto.response;

import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.valueobject.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CaseResponse(
        UUID id,
        String caseNumber,
        String title,
        CaseCategory category,
        CaseStatus status,
        Priority priority,
        RiskLevel riskLevel,
        String tin,
        String taxpayerName,
        String entityType,
        UUID createdById,
        String createdByName,
        Instant createdAt,
        UUID assigningOfficerId,
        String assigningOfficerName,
        UUID assignedOfficerId,
        String assignedOfficerName,
        String assignedUnitId,
        String assignedUnitName,
        Instant assignedDate,
        String assignmentReason,
        boolean selfAssigned,
        Instant deadline,
        int targetDays,
        Instant lastActivityAt,
        BigDecimal estimatedAmountEtb,
        BigDecimal assessedAmountEtb,
        BigDecimal collectedAmountEtb,
        BigDecimal writtenOffAmountEtb,
        BigDecimal refundAmountEtb,
        String reasons,
        String instructions,
        String remarks,
        Map<String, Object> typeSpecificData,
        String currentApprovalLevel,
        int noteCount,
        int documentCount,
        int taskCount
) {
    public static CaseResponse from(Case c) {
        return new CaseResponse(
                c.getId(), c.getCaseNumber(), c.getTitle(),
                c.getCategory(), c.getStatus(), c.getPriority(), c.getRiskLevel(),
                c.getTin().value(), c.getTaxpayerName(), c.getEntityType(),
                c.getCreatedById(), c.getCreatedByName(), c.getCreatedAt(),
                c.getAssigningOfficerId(), c.getAssigningOfficerName(),
                c.getAssignedOfficerId(), c.getAssignedOfficerName(),
                c.getAssignedUnitId(), c.getAssignedUnitName(),
                c.getAssignedDate(), c.getAssignmentReason(), c.isSelfAssigned(),
                c.getDeadline(), c.getTargetDays(), c.getLastActivityAt(),
                c.getEstimatedAmountEtb().value(), c.getAssessedAmountEtb().value(),
                c.getCollectedAmountEtb().value(), c.getWrittenOffAmountEtb().value(),
                c.getRefundAmountEtb().value(),
                c.getReasons(), c.getInstructions(), c.getRemarks(),
                c.getTypeSpecificData(),
                c.getCurrentApprovalLevel() != null ? c.getCurrentApprovalLevel().name() : null,
                c.getNotes().size(), c.getDocuments().size(), c.getTaskReminders().size()
        );
    }
}
