package com.act.casemanagement.application.usecase.notes;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.application.port.EventPublisherPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ForbiddenOperationException;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.model.CaseNote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Section 5.2: only the note's original author or the case's assigning officer
 * may mark a note obsolete.
 */
@Service
@RequiredArgsConstructor
public class MarkNoteObsoleteUseCase {

    private final CaseRepositoryPort caseRepository;
    private final EventPublisherPort eventPublisher;

    @Transactional
    public CaseNote execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        CaseNote note = aCase.findNote(cmd.noteId());

        boolean isAuthor          = note.getAuthorId().equals(actor.getActorId());
        boolean isAssigningOfficer = aCase.getAssigningOfficerId() != null
                && aCase.getAssigningOfficerId().equals(actor.getActorId());

        if (!isAuthor && !isAssigningOfficer) {
            throw new ForbiddenOperationException(
                    actor.getActorId(), "MARK_NOTE_OBSOLETE", cmd.noteId());
        }

        aCase.markNoteObsolete(cmd.noteId(), actor.getActorId(), cmd.reason());

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
        return saved.findNote(cmd.noteId());
    }

    public record Command(UUID caseId, UUID noteId, String reason) {}
}
