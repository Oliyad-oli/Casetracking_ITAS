package com.act.casemanagement.api.dto.request;

import com.act.casemanagement.application.usecase.cases.CreateCaseUseCase;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.Priority;
import com.act.casemanagement.domain.valueobject.RiskLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * No actorId field — actor identity comes from X-Authenticated-Actor-Id header only.
 */
public record CreateCaseRequest(
        @NotBlank  String title,
        @NotNull   CaseCategory category,
        @NotNull   Priority priority,
        @NotNull   RiskLevel riskLevel,
        @NotBlank  String tin,
        @NotBlank  String taxpayerName,
                   String entityType,
                   String createdByName,
                   Instant deadline,
                   int targetDays,
                   BigDecimal estimatedAmountEtb,
                   String reasons,
                   String instructions,
                   String remarks,
                   Map<String, Object> typeSpecificData
) {
    public CreateCaseUseCase.Command toCommand() {
        return new CreateCaseUseCase.Command(
                title, category, priority, riskLevel, tin, taxpayerName,
                entityType, createdByName, deadline,
                targetDays > 0 ? targetDays : 45,
                estimatedAmountEtb, reasons, instructions, remarks, typeSpecificData);
    }
}
