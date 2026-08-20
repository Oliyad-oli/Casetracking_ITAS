package com.act.casemanagement.application.usecase.assignment;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.*;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReassignCaseUseCase {

    private final CaseRepositoryPort  caseRepository;
    private final UserRepositoryPort  userRepository;
    private final EventPublisherPort  eventPublisher;

    @Transactional
    public Case execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
            .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        boolean isSelf = cmd.newOfficerId().equals(actor.getActorId());
        if (isSelf && (cmd.reason() == null || cmd.reason().isBlank())) {
            throw new DomainException(
                "Self-reassignment requires a mandatory justification (CTR0200/CTR0300)");
        }

        var officer = userRepository.findById(cmd.newOfficerId())
            .orElseThrow(() -> new ResourceNotFoundException("Officer", cmd.newOfficerId()));

        // Reassign: same as assign — aggregate handles self-detection
        aCase.assign(
            cmd.newOfficerId(), officer.name(),
            officer.unitId(), officer.unit(),
            actor.getActorId(), cmd.reassigningActorName(),
            cmd.reason()
        );

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
        return saved;
    }

    public record Command(UUID caseId, UUID newOfficerId,
                          String reassigningActorName, String reason) {}
}
