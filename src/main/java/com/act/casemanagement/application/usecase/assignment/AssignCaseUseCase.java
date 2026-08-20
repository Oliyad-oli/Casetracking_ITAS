package com.act.casemanagement.application.usecase.assignment;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.*;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.service.AutomaticTransitionService;
import com.act.casemanagement.domain.service.AutomaticTransitionService.Trigger;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import com.act.casemanagement.observability.audit.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * CTR0200 / CTR0300 — Assign a case to an officer.
 * <p>
 * Section 5.2 authorization: if actorId == officerId (self-assignment), a
 * non-blank justification is required AND the aggregate emits CaseSelfAssigned
 * (distinct from CaseAssigned).
 * <p>
 * Automatic workflow transition is evaluated after assignment (OFFICER_ASSIGNED trigger).
 */
@Service
@RequiredArgsConstructor
public class AssignCaseUseCase {

    private final CaseRepositoryPort           caseRepository;
    private final UserRepositoryPort           userRepository;
    private final WorkflowStageRepositoryPort  workflowStageRepository;
    private final EventPublisherPort           eventPublisher;
    private final AutomaticTransitionService   transitionService;

    @Auditable(action = "ASSIGN_CASE")
    @Transactional
    public Case execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
            .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        // Self-assignment: mandatory justification (CTR0200/CTR0300 — Section 5.2)
        boolean isSelf = cmd.officerId().equals(actor.getActorId());
        if (isSelf && (cmd.reason() == null || cmd.reason().isBlank())) {
            throw new DomainException(
                "Self-assignment requires a mandatory justification (CTR0200/CTR0300)");
        }

        var officer = userRepository.findById(cmd.officerId())
            .orElseThrow(() -> new ResourceNotFoundException("Officer", cmd.officerId()));

        aCase.assign(
            cmd.officerId(), officer.name(),
            officer.unitId(), officer.unit(),
            actor.getActorId(), cmd.assigningActorName(),
            cmd.reason()
        );

        // Automatic workflow transition
        var stages = workflowStageRepository.findAllActive();
        Optional<CaseStatus> nextStatus = transitionService.computeTransition(
            Trigger.OFFICER_ASSIGNED, aCase.getStatus(), aCase.getCategory(), stages);
        nextStatus.ifPresent(s -> aCase.transitionStatus(s, actor.getActorId(),
            "Auto-transition on officer assignment"));

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
        return saved;
    }

    public record Command(
        UUID caseId,
        UUID officerId,
        String assigningActorName,
        String reason
    ) {}
}
