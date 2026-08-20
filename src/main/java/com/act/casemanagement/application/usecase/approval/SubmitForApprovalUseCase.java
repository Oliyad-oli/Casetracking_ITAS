package com.act.casemanagement.application.usecase.approval;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.*;
import com.act.casemanagement.domain.aggregate.ApprovalMatrixConfig;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.service.ApprovalEscalationService;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitForApprovalUseCase {

    private final CaseRepositoryPort          caseRepository;
    private final ApprovalMatrixRepositoryPort matrixRepository;
    private final EventPublisherPort          eventPublisher;
    private final ApprovalEscalationService   escalationService;

    @Transactional
    public Case execute(UUID caseId, String comments) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId));

        List<ApprovalMatrixConfig> rules = matrixRepository.findAllActive();
        List<ApprovalLevel> chain = escalationService.requiredChain(
                aCase.getCategory(), aCase.getRiskLevel(),
                aCase.getEstimatedAmountEtb().value(), rules);

        ApprovalLevel firstLevel = chain.isEmpty() ? ApprovalLevel.SUPERVISOR : chain.get(0);
        aCase.submitForApproval(firstLevel, actor.getActorId(), comments);

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
        return saved;
    }
}
