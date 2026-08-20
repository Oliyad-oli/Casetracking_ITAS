package com.act.casemanagement.application.usecase.association;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.application.port.EventPublisherPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.event.CasesAssociated;
import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.valueobject.AssociationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssociateCasesUseCase {

    private final CaseRepositoryPort caseRepository;
    private final EventPublisherPort eventPublisher;

    @Transactional
    public void execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        if (cmd.sourceCaseId().equals(cmd.targetCaseId())) {
            throw new DomainException("Cannot associate a case with itself");
        }

        Case source = caseRepository.findById(cmd.sourceCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.sourceCaseId()));

        // Verify target exists
        Case target = caseRepository.findById(cmd.targetCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.targetCaseId()));

        // Register the association event on the source aggregate
        source.registerEvent(new CasesAssociated(
                UUID.randomUUID(), Instant.now(),
                cmd.sourceCaseId(), cmd.targetCaseId(),
                cmd.relationshipType(), actor.getActorId()));

        // Add activity note on source
        source.addNote(
                UUID.randomUUID(),
                "[CASE ASSOCIATED] Linked to " + target.getCaseNumber()
                        + " as " + cmd.relationshipType()
                        + ". Notes: " + cmd.notes(),
                java.time.LocalDate.now(),
                actor.getActorId(), cmd.actorName(),
                actor.getRole(), null);

        Case saved = caseRepository.save(source);
        saved.pullEvents().forEach(eventPublisher::publish);
    }

    public record Command(
            UUID sourceCaseId,
            UUID targetCaseId,
            AssociationType relationshipType,
            String notes,
            String actorName
    ) {}
}
