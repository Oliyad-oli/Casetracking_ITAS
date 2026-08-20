package com.act.casemanagement.domain.service;

import com.act.casemanagement.domain.aggregate.WorkflowStageConfig;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;

import java.util.List;
import java.util.Optional;

/**
 * Domain service — ports the workflow-stage-driven automatic transition logic
 * from AppContext.tsx {@code evaluateAutomaticTransitions()}.
 * <p>
 * Triggered by: DOC_UPLOAD (DocumentAdded event), OFFICER_ASSIGNED (CaseAssigned event).
 * Falls back gracefully when no matching stage is found.
 */
public class AutomaticTransitionService {

    public enum Trigger { DOC_UPLOAD, OFFICER_ASSIGNED }

    /**
     * Computes the next status (if any) for the given trigger, current status,
     * and category-filtered workflow stages.
     *
     * @return the new status, or empty if no transition should occur
     */
    public Optional<CaseStatus> computeTransition(
            Trigger trigger,
            CaseStatus currentStatus,
            CaseCategory category,
            List<WorkflowStageConfig> allActiveStages) {

        List<WorkflowStageConfig> pipeline = allActiveStages.stream()
            .filter(s -> s.getCategory() == null || s.getCategory() == category)
            .toList();

        return switch (trigger) {
            case DOC_UPLOAD -> {
                Optional<CaseStatus> fromStage = pipeline.stream()
                    .filter(s -> s.getStatus() == currentStatus
                        || (s.getAllowedActions() != null && s.getAllowedActions().contains("VERIFY_DOCS"))
                        || s.getStatus() == CaseStatus.OPEN || s.getStatus() == CaseStatus.DRAFT)
                    .filter(s -> s.getNextStatus() != null && s.getNextStatus() != currentStatus)
                    .map(WorkflowStageConfig::getNextStatus)
                    .findFirst();

                if (fromStage.isPresent()) {
                    yield fromStage;
                }
                // Fallback: OPEN → UNASSIGNED on doc upload
                if (currentStatus == CaseStatus.OPEN) {
                    yield Optional.of(CaseStatus.UNASSIGNED);
                }
                yield Optional.empty();
            }
            case OFFICER_ASSIGNED -> {
                Optional<CaseStatus> fromStage = pipeline.stream()
                    .filter(s -> s.getStatus() == currentStatus
                        || s.getStatus() == CaseStatus.UNASSIGNED
                        || (s.getAllowedActions() != null && s.getAllowedActions().contains("ASSIGN_CASE")))
                    .filter(s -> s.getNextStatus() != null && s.getNextStatus() != currentStatus)
                    .map(WorkflowStageConfig::getNextStatus)
                    .findFirst();

                if (fromStage.isPresent()) {
                    yield fromStage;
                }
                // Fallback: UNASSIGNED/OPEN → ASSIGNED
                if (currentStatus == CaseStatus.UNASSIGNED || currentStatus == CaseStatus.OPEN) {
                    yield Optional.of(CaseStatus.ASSIGNED);
                }
                yield Optional.empty();
            }
        };
    }
}
