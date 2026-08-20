package com.act.casemanagement.domain.service;

import com.act.casemanagement.domain.aggregate.ApprovalMatrixConfig;
import com.act.casemanagement.domain.valueobject.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain service — ports exact escalation logic from the frontend's
 * {@code processApproval()} in AppContext.tsx.
 * <p>
 * Matching order: walk all active ApprovalMatrixConfig rules; first match wins.
 * Match criteria: caseType (null=ALL), riskLevel (null=ALL), value range.
 * Default chain when no rule matches: [SUPERVISOR, MANAGER].
 */
public class ApprovalEscalationService {

    private static final List<ApprovalLevel> DEFAULT_CHAIN =
        List.of(ApprovalLevel.SUPERVISOR, ApprovalLevel.MANAGER);

    /**
     * Returns the full required approval chain for the given case attributes,
     * consulting the active matrix rules. SENIOR_MANAGER is never collapsed
     * with MANAGER in any rule.
     */
    public List<ApprovalLevel> requiredChain(
            CaseCategory category,
            RiskLevel riskLevel,
            BigDecimal caseAmountEtb,
            List<ApprovalMatrixConfig> activeRules) {

        return activeRules.stream()
            .filter(r -> typeMatches(r.getCaseType(), category))
            .filter(r -> riskMatches(r.getRiskLevel(), riskLevel))
            .filter(r -> valueMatches(r.getMinValueEtb(), r.getMaxValueEtb(), caseAmountEtb))
            .findFirst()
            .map(ApprovalMatrixConfig::getRequiredApprovals)
            .orElse(DEFAULT_CHAIN);
    }

    /**
     * Given the current approver's level and the action, computes the next
     * status and next required approval level.
     */
    public ApprovalOutcome computeOutcome(
            ApprovalAction action,
            ApprovalLevel currentActorLevel,
            List<ApprovalLevel> requiredChain) {

        return switch (action) {
            case APPROVED -> {
                int idx = requiredChain.indexOf(currentActorLevel);
                if (idx >= 0 && idx < requiredChain.size() - 1) {
                    ApprovalLevel next = requiredChain.get(idx + 1);
                    yield new ApprovalOutcome(next.toPendingStatus(), next);
                }
                yield new ApprovalOutcome(CaseStatus.APPROVED, ApprovalLevel.NONE);
            }
            case REQUESTED_INFO -> new ApprovalOutcome(CaseStatus.PENDING_INFO, ApprovalLevel.NONE);
            case REJECTED       -> new ApprovalOutcome(CaseStatus.REJECTED, ApprovalLevel.NONE);
            case ESCALATED -> {
                int idx = requiredChain.indexOf(currentActorLevel);
                ApprovalLevel next = (idx >= 0 && idx < requiredChain.size() - 1)
                    ? requiredChain.get(idx + 1)
                    : ApprovalLevel.COMMISSIONER;
                yield new ApprovalOutcome(CaseStatus.ESCALATED, next);
            }
        };
    }

    public record ApprovalOutcome(CaseStatus newStatus, ApprovalLevel nextLevel) {}

    // ─── matching helpers ───

    private boolean typeMatches(CaseCategory ruleType, CaseCategory caseType) {
        return ruleType == null || ruleType == caseType;
    }

    private boolean riskMatches(RiskLevel ruleRisk, RiskLevel caseRisk) {
        return ruleRisk == null || ruleRisk == caseRisk;
    }

    private boolean valueMatches(BigDecimal min, BigDecimal max, BigDecimal amount) {
        if (amount == null) return true;
        if (min != null && amount.compareTo(min) < 0) return false;
        if (max != null && max.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(max) > 0) return false;
        return true;
    }
}
