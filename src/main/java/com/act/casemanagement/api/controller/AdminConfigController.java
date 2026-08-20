package com.act.casemanagement.api.controller;

import com.act.casemanagement.application.usecase.admin.*;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import com.act.casemanagement.domain.valueobject.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CTR1100 — Admin configuration endpoints.
 * Case type config, workflow stages, approval matrix, assignment weights.
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "CTR1100", description = "Admin Configuration (CTR1100)")
@RequiredArgsConstructor
public class AdminConfigController {

    private final SaveCaseTypeConfigUseCase     caseTypeConfigUseCase;
    private final SaveApprovalMatrixUseCase     approvalMatrixUseCase;
    private final SaveWorkflowStagesUseCase     workflowStagesUseCase;
    private final SaveAssignmentWeightsUseCase  assignmentWeightsUseCase;

    // ── Case Type Config ───────────────────────────────────────────────────

    @PostMapping("/case-types")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save / update a case type configuration")
    public void saveCaseTypeConfig(@Valid @RequestBody CaseTypeConfigBody body) {
        caseTypeConfigUseCase.execute(new SaveCaseTypeConfigUseCase.Command(
                body.id(), body.code(), body.name(), body.description(), body.active(),
                body.effectiveDate(), body.configVersion(), body.defaultTargetDays(),
                body.fieldsDefinition(), body.allowedStatuses(), body.approvalLevels()));
    }

    // ── Approval Matrix ────────────────────────────────────────────────────

    @PostMapping("/approval-matrix")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Replace the entire approval matrix (CTR0800)")
    public void saveApprovalMatrix(@Valid @RequestBody List<ApprovalMatrixRuleBody> rules) {
        approvalMatrixUseCase.execute(rules.stream()
                .map(r -> new SaveApprovalMatrixUseCase.RuleCommand(
                        r.id(), r.caseType(), r.minValueEtb(), r.maxValueEtb(),
                        r.riskLevel() != null
                            ? com.act.casemanagement.domain.valueobject.RiskLevel.valueOf(r.riskLevel()) : null,
                        r.requiredApprovals()))
                .toList());
    }

    // ── Workflow Stages ────────────────────────────────────────────────────

    @PostMapping("/workflow-stages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Replace all workflow stage definitions")
    public void saveWorkflowStages(@Valid @RequestBody List<WorkflowStageBody> stages) {
        workflowStagesUseCase.execute(stages.stream()
                .map(s -> new SaveWorkflowStagesUseCase.StageCommand(
                        s.id(), s.name(), s.category(), s.description(), s.status(),
                        s.allowedRoles(), s.allowedActions(), s.slaHours(), s.nextStatus()))
                .toList());
    }

    // ── Assignment Weights ─────────────────────────────────────────────────

    @PostMapping("/assignment-weights")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save new assignment scoring weights (must sum to 1.0)")
    public void saveAssignmentWeights(@Valid @RequestBody AssignmentWeightsBody body) {
        assignmentWeightsUseCase.execute(new SaveAssignmentWeightsUseCase.Command(
                body.capacityWeight(), body.specializationWeight(),
                body.regionWeight(), body.caseloadMixWeight()));
    }

    // ── Inner request record types ─────────────────────────────────────────

    record CaseTypeConfigBody(
            UUID id, @NotNull CaseCategory code, @NotNull String name, String description,
            boolean active, @NotNull LocalDate effectiveDate, String configVersion,
            int defaultTargetDays, List<Map<String, Object>> fieldsDefinition,
            List<CaseStatus> allowedStatuses, List<ApprovalLevel> approvalLevels) {}

    record ApprovalMatrixRuleBody(
            UUID id, CaseCategory caseType,
            @NotNull BigDecimal minValueEtb, BigDecimal maxValueEtb,
            String riskLevel, @NotNull List<ApprovalLevel> requiredApprovals) {}

    record WorkflowStageBody(
            UUID id, @NotNull String name, CaseCategory category, String description,
            @NotNull CaseStatus status, @NotNull List<UserRole> allowedRoles,
            List<String> allowedActions, int slaHours, CaseStatus nextStatus) {}

    record AssignmentWeightsBody(
            @NotNull BigDecimal capacityWeight,
            @NotNull BigDecimal specializationWeight,
            @NotNull BigDecimal regionWeight,
            @NotNull BigDecimal caseloadMixWeight) {}
}
