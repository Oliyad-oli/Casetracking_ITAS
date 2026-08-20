package com.act.casemanagement.application.usecase.tasks;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.application.port.EventPublisherPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelTaskReminderUseCase {

    private final CaseRepositoryPort caseRepository;
    private final EventPublisherPort eventPublisher;

    @Transactional
    public void execute(UUID caseId, UUID taskId, String reason) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId));

        aCase.cancelTask(taskId, actor.getActorId(), reason);

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
    }
}
