package com.act.casemanagement.application.usecase.assignment;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.*;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.observability.audit.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeassignCaseUseCase {

    private final CaseRepositoryPort caseRepository;
    private final EventPublisherPort eventPublisher;

    @Auditable(action = "DEASSIGN_CASE")
    @Transactional
    public Case execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
            .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        aCase.deassign(actor.getActorId(), cmd.reason());

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
        return saved;
    }

    public record Command(UUID caseId, String reason) {}
}
