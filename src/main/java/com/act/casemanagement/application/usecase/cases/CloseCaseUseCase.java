package com.act.casemanagement.application.usecase.cases;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.application.port.EventPublisherPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ForbiddenOperationException;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.valueobject.CaseClosureDetails;
import com.act.casemanagement.domain.valueobject.MonetaryAmount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * CTR0700 — Close a case.
 * <p>
 * Section 5.2 authorization: only the assigning officer OR a directorate-override
 * role (MANAGER, SENIOR_MANAGER, COMMISSIONER) may close.
 * <p>
 * Rejection path: ForbiddenOperationException is thrown, but BEFORE throwing we
 * register an UnauthorizedClosureAttempted event on the aggregate so the
 * AuditInterceptor captures the FAILURE with full before/after context.
 */
@Service
@RequiredArgsConstructor
public class CloseCaseUseCase {

    private final CaseRepositoryPort caseRepository;
    private final EventPublisherPort eventPublisher;

    @Transactional
    public Case execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        boolean isAssigningOfficer = aCase.getAssigningOfficerId() != null
                && aCase.getAssigningOfficerId().equals(actor.getActorId());
        boolean isDirectorateOverride = actor.getRole().isDirectorateOverride();

        if (!isAssigningOfficer && !isDirectorateOverride) {
            // Register the attempt event before throwing so it is captured in the
            // FAILURE audit entry by AuditInterceptor (Section 5.3)
            aCase.recordUnauthorizedClosureAttempt(
                    actor.getActorId(), actor.getRole().name());
            caseRepository.save(aCase);
            aCase.pullEvents().forEach(eventPublisher::publish);

            throw new ForbiddenOperationException(
                    actor.getActorId(), "CLOSE_CASE", cmd.caseId());
        }

        CaseClosureDetails closure = new CaseClosureDetails(
                cmd.closureReason(),
                cmd.outcome(),
                cmd.resolutionSummary(),
                money(cmd.amountAssessed()),
                money(cmd.amountCollected()),
                money(cmd.amountWrittenOff()),
                money(cmd.refundApproved()),
                actor.getActorId(),
                cmd.closedByName(),
                Instant.now(),
                cmd.finalDocumentId()
        );

        aCase.close(closure);

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
        return saved;
    }

    private MonetaryAmount money(BigDecimal v) {
        return MonetaryAmount.of(v != null ? v : BigDecimal.ZERO);
    }

    public record Command(
            UUID caseId,
            CaseClosureDetails.ClosureReason closureReason,
            CaseClosureDetails.ClosureOutcome outcome,
            String resolutionSummary,
            BigDecimal amountAssessed,
            BigDecimal amountCollected,
            BigDecimal amountWrittenOff,
            BigDecimal refundApproved,
            String closedByName,
            String finalDocumentId
    ) {}
}
