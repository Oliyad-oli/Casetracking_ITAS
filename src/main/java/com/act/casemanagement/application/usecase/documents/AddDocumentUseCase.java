package com.act.casemanagement.application.usecase.documents;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.application.port.DmsPort;
import com.act.casemanagement.application.port.EventPublisherPort;
import com.act.casemanagement.application.port.WorkflowStageRepositoryPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.model.CaseDocument;
import com.act.casemanagement.domain.service.AutomaticTransitionService;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddDocumentUseCase {

    private final CaseRepositoryPort          caseRepository;
    private final DmsPort                     dmsPort;
    private final WorkflowStageRepositoryPort workflowStageRepository;
    private final EventPublisherPort          eventPublisher;
    private final AutomaticTransitionService  transitionService;

    @Transactional
    public CaseDocument execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        // Store in DMS (stub returns a reference)
        String dmsRef = null;
        if (cmd.content() != null && cmd.content().length > 0) {
            dmsRef = dmsPort.store(cmd.caseId(), cmd.fileName(),
                    cmd.contentType(), cmd.content(), UUID.randomUUID());
        }

        CaseDocument doc = aCase.addDocument(
                UUID.randomUUID(),
                cmd.fileName(),
                cmd.documentType(),
                cmd.fileType(),
                cmd.sizeInMb(),
                actor.getActorId(),
                cmd.uploaderName(),
                cmd.classification(),
                cmd.docVersion(),
                dmsRef
        );

        // Automatic workflow transition on doc upload
        var stages = workflowStageRepository.findAllActive();
        Optional<CaseStatus> nextStatus = transitionService.computeTransition(
                AutomaticTransitionService.Trigger.DOC_UPLOAD,
                aCase.getStatus(), aCase.getCategory(), stages);
        nextStatus.ifPresent(s -> aCase.transitionStatus(s, actor.getActorId(),
                "Auto-transition on document upload"));

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);

        return saved.getDocuments().stream()
                .filter(d -> d.getId().equals(doc.getId()))
                .findFirst()
                .orElse(doc);
    }

    public record Command(
            UUID caseId,
            String fileName,
            String fileType,
            String contentType,
            Double sizeInMb,
            CaseDocument.DocumentType documentType,
            CaseDocument.SecurityClassification classification,
            String docVersion,
            String uploaderName,
            byte[] content          // may be null if only registering a DMS reference
    ) {}
}
