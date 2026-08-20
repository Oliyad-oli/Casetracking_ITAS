package com.act.casemanagement.application.usecase.notes;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.*;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ForbiddenOperationException;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.model.CaseNote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * CTR0500 — Edit a case note.
 * <p>
 * Section 5.2 authorization: only the note's original author OR the case's
 * assigning officer may edit. Rejection throws ForbiddenOperationException
 * which is captured as a FAILURE audit entry by AuditInterceptor.
 */
@Service
@RequiredArgsConstructor
public class UpdateCaseNoteUseCase {

    private final CaseRepositoryPort caseRepository;
    private final EventPublisherPort eventPublisher;

    @Transactional
    public CaseNote execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
            .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        CaseNote note = aCase.findNote(cmd.noteId());

        // Authorization check (Section 5.2)
        boolean isAuthor          = note.getAuthorId().equals(actor.getActorId());
        boolean isAssigningOfficer = aCase.getAssigningOfficerId() != null
            && aCase.getAssigningOfficerId().equals(actor.getActorId());

        if (!isAuthor && !isAssigningOfficer) {
            throw new ForbiddenOperationException(
                actor.getActorId(), "UPDATE_CASE_NOTE", cmd.noteId());
        }

        aCase.editNote(cmd.noteId(), cmd.newContent(), actor.getActorId(),
            cmd.editorName(), cmd.reason());

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);

        return saved.findNote(cmd.noteId());
    }

    public record Command(UUID caseId, UUID noteId,
                          String newContent, String editorName, String reason) {}
}
