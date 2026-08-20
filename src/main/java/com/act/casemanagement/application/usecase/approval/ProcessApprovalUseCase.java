package com.act.casemanagement.application.usecase.approval;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.*;
import com.act.casemanagement.domain.aggregate.ApprovalMatrixConfig;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.service.ApprovalEscalationService;
import com.act.casemanagement.domain.valueobject.ApprovalAction;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * CTR0800 — Process an approval decision.
 * Ports the exact approval-chain logic from AppContext.tsx processApproval(),
 * delegating to ApprovalEscalationService for matrix lookup.
 */
@Service
@RequiredArgsConstructor
public class ProcessApprovalUseCase {

    private final CaseRepositoryPort          caseRepository;
    private final ApprovalMatrixRepositoryPort matrixRepository;
    private final EventPublisherPort          eventPublisher;
    private final ApprovalEscalationService   escalationService;

    @Transactional
    public Case execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        List<ApprovalMatrixConfig> activeRules = matrixRepository.findAllActive();

        List<ApprovalLevel> chain = escalationService.requiredChain(
                aCase.getCategory(),
                aCase.getRiskLevel(),
                aCase.getAssessedAmountEtb() != null
                        ? aCase.getAssessedAmountEtb().value()
                        : aCase.getEstimatedAmountEtb().value(),
                activeRules
        );

        // Derive approver's level from their role
        ApprovalLevel approverLevel = switch (actor.getRole()) {
            case SUPERVISOR     -> ApprovalLevel.SUPERVISOR;
            case MANAGER        -> ApprovalLevel.MANAGER;
            case SENIOR_MANAGER -> ApprovalLevel.SENIOR_MANAGER;
            case COMMISSIONER   -> ApprovalLevel.COMMISSIONER;
            default             -> ApprovalLevel.SUPERVISOR;
        };

        ApprovalEscalationService.ApprovalOutcome outcome =
                escalationService.computeOutcome(cmd.action(), approverLevel, chain);

        aCase.processApproval(
                approverLevel,
                actor.getActorId(),
                cmd.approverName(),
                actor.getRole(),
                cmd.action(),
                cmd.comments(),
                outcome.nextLevel(),
                outcome.newStatus()
        );

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
        return saved;
    }

    public record Command(
            UUID caseId,
            ApprovalAction action,
            String approverName,
            String comments
    ) {}
}
