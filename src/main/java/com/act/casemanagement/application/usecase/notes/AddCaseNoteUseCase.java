package com.act.casemanagement.application.usecase.notes;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.*;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.model.CaseNote;
import com.act.casemanagement.domain.valueobject.UserRole;
import com.act.casemanagement.observability.audit.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddCaseNoteUseCase {

    private final CaseRepositoryPort caseRepository;
    private final EventPublisherPort eventPublisher;

    @Auditable(action = "ADD_CASE_NOTE")
    @Transactional
    public CaseNote execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
            .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        CaseNote note = aCase.addNote(
            UUID.randomUUID(),
            cmd.content(),
            cmd.eventDate() != null ? cmd.eventDate() : LocalDate.now(),
            actor.getActorId(),
            cmd.authorName(),
            actor.getRole(),
            cmd.attachmentName()
        );

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);

        // Return the note from the saved aggregate (preserves any version set by persistence)
        return saved.getNotes().stream()
            .filter(n -> n.getId().equals(note.getId()))
            .findFirst()
            .orElse(note);
    }

    public record Command(
        UUID caseId,
        String content,
        LocalDate eventDate,
        String authorName,
        String attachmentName
    ) {}
}
