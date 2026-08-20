package com.act.casemanagement.persistence.adapter;

import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.model.ApprovalRecord;
import com.act.casemanagement.domain.model.CaseDocument;
import com.act.casemanagement.domain.model.CaseNote;
import com.act.casemanagement.domain.model.TaskReminder;
import com.act.casemanagement.domain.valueobject.*;
import com.act.casemanagement.persistence.jpa.entity.*;
import com.act.casemanagement.persistence.jpa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Bridges Case domain aggregate ↔ JPA entities.
 * NOT @Transactional — the use case owns the transaction boundary.
 * Carries @Version from entity into domain and back on save to prevent
 * the Hibernate "different object with same identifier" optimistic-lock error.
 */
@Component
@RequiredArgsConstructor
public class CasePersistenceAdapter implements CaseRepositoryPort {

    private final CaseJpaRepository         caseRepo;
    private final CaseNoteJpaRepository     noteRepo;
    private final CaseDocumentJpaRepository docRepo;
    private final ApprovalRecordJpaRepository approvalRepo;
    private final TaskReminderJpaRepository  taskRepo;

    @Override
    public Case save(Case aCase) {
        CaseEntity entity = toEntity(aCase);
        CaseEntity saved  = caseRepo.save(entity);
        // Persist child collections
        saveNotes(aCase.notesInternal());
        saveDocs(aCase.documentsInternal());
        saveApprovals(aCase.approvalRecordsInternal());
        saveTasks(aCase.taskRemindersInternal());
        // Carry persisted version back into domain aggregate
        aCase.setVersion(saved.getVersion());
        return aCase;
    }

    @Override
    public Optional<Case> findById(UUID id) {
        return caseRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Case> findByCaseNumber(String caseNumber) {
        return caseRepo.findByCaseNumber(caseNumber).map(this::toDomain);
    }

    @Override
    public List<Case> findByAssignedOfficerId(UUID officerId) {
        return caseRepo.findByAssignedOfficerId(officerId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Case> findByAssignedUnitId(String unitId) {
        return caseRepo.findByAssignedUnitId(unitId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Case> findByStatus(CaseStatus status) {
        return caseRepo.findByStatus(status.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Case> findByTin(String tin) {
        return caseRepo.findByTin(tin).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Case> findAll() {
        return caseRepo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Case> findAllActive() {
        return caseRepo.findAllActive().stream().map(this::toDomain).toList();
    }

    @Override
    public long countActiveByOfficerId(UUID officerId) {
        return caseRepo.countActiveByOfficerId(officerId);
    }

    @Override
    public long countHighRiskActiveByOfficerId(UUID officerId) {
        return caseRepo.countHighRiskActiveByOfficerId(officerId);
    }

    // ── entity → domain ──────────────────────────────────────────────────────

    private Case toDomain(CaseEntity e) {
        List<CaseNote>       notes     = noteRepo.findByCaseId(e.getId()).stream()
                                             .map(this::noteToDomain).toList();
        List<CaseDocument>   docs      = docRepo.findByCaseId(e.getId()).stream()
                                             .map(this::docToDomain).toList();
        List<ApprovalRecord> approvals = approvalRepo.findByCaseId(e.getId()).stream()
                                             .map(this::approvalToDomain).toList();
        List<TaskReminder>   tasks     = taskRepo.findByCaseId(e.getId()).stream()
                                             .map(this::taskToDomain).toList();

        CaseClosureDetails closure = null;
        if (e.getClosureReason() != null) {
            closure = new CaseClosureDetails(
                CaseClosureDetails.ClosureReason.valueOf(e.getClosureReason()),
                e.getClosureOutcome() != null
                    ? CaseClosureDetails.ClosureOutcome.valueOf(e.getClosureOutcome()) : null,
                e.getClosureSummary(),
                MonetaryAmount.of(e.getClosureAmountAssessed() != null ? e.getClosureAmountAssessed()
                    : java.math.BigDecimal.ZERO),
                MonetaryAmount.of(e.getClosureAmountCollected() != null ? e.getClosureAmountCollected()
                    : java.math.BigDecimal.ZERO),
                MonetaryAmount.of(e.getClosureAmountWrittenOff() != null ? e.getClosureAmountWrittenOff()
                    : java.math.BigDecimal.ZERO),
                MonetaryAmount.of(e.getClosureRefundApproved() != null ? e.getClosureRefundApproved()
                    : java.math.BigDecimal.ZERO),
                e.getClosedById(), e.getClosedByName(), e.getClosedAt(), e.getClosureDocId()
            );
        }

        return Case.reconstitute(
            e.getId(), e.getVersion(), e.getCaseNumber(), e.getTitle(),
            CaseCategory.valueOf(e.getCategory()), CaseStatus.valueOf(e.getStatus()),
            Priority.valueOf(e.getPriority()), RiskLevel.valueOf(e.getRiskLevel()),
            new TIN(e.getTin()), e.getTaxpayerName(), e.getEntityType(),
            e.getCreatedById(), e.getCreatedByName(), e.getCreatedAt(),
            e.getAssigningOfficerId(), e.getAssigningOfficerName(),
            e.getAssignedOfficerId(), e.getAssignedOfficerName(),
            e.getAssignedUnitId(), e.getAssignedUnitName(),
            e.getAssignedDate(), e.getAssignmentReason(), e.isSelfAssigned(),
            e.getDeadline(), e.getTargetDays(), e.getLastActivityAt(),
            MonetaryAmount.of(e.getEstimatedAmountEtb()),
            MonetaryAmount.of(e.getAssessedAmountEtb()),
            MonetaryAmount.of(e.getCollectedAmountEtb()),
            MonetaryAmount.of(e.getWrittenOffAmountEtb()),
            MonetaryAmount.of(e.getRefundAmountEtb()),
            e.getReasons(), e.getInstructions(), e.getRemarks(),
            e.getTypeSpecificData(),
            e.getCurrentApprovalLevel() != null
                ? ApprovalLevel.valueOf(e.getCurrentApprovalLevel()) : ApprovalLevel.NONE,
            closure, e.isDraft(),
            notes, docs, approvals, tasks
        );
    }

    // ── domain → entity ──────────────────────────────────────────────────────

    private CaseEntity toEntity(Case c) {
        CaseEntity e = new CaseEntity();
        e.setId(c.getId());
        e.setVersion(c.getVersion());
        e.setCaseNumber(c.getCaseNumber());
        e.setTitle(c.getTitle());
        e.setCategory(c.getCategory().name());
        e.setStatus(c.getStatus().name());
        e.setPriority(c.getPriority().name());
        e.setRiskLevel(c.getRiskLevel().name());
        e.setTin(c.getTin().value());
        e.setTaxpayerName(c.getTaxpayerName());
        e.setEntityType(c.getEntityType());
        e.setCreatedById(c.getCreatedById());
        e.setCreatedByName(c.getCreatedByName());
        e.setCreatedAt(c.getCreatedAt());
        e.setAssigningOfficerId(c.getAssigningOfficerId());
        e.setAssigningOfficerName(c.getAssigningOfficerName());
        e.setAssignedOfficerId(c.getAssignedOfficerId());
        e.setAssignedOfficerName(c.getAssignedOfficerName());
        e.setAssignedUnitId(c.getAssignedUnitId());
        e.setAssignedUnitName(c.getAssignedUnitName());
        e.setAssignedDate(c.getAssignedDate());
        e.setAssignmentReason(c.getAssignmentReason());
        e.setSelfAssigned(c.isSelfAssigned());
        e.setDeadline(c.getDeadline());
        e.setTargetDays(c.getTargetDays());
        e.setLastActivityAt(c.getLastActivityAt());
        e.setReasons(c.getReasons());
        e.setInstructions(c.getInstructions());
        e.setRemarks(c.getRemarks());
        e.setEstimatedAmountEtb(c.getEstimatedAmountEtb().value());
        e.setAssessedAmountEtb(c.getAssessedAmountEtb().value());
        e.setCollectedAmountEtb(c.getCollectedAmountEtb().value());
        e.setWrittenOffAmountEtb(c.getWrittenOffAmountEtb().value());
        e.setRefundAmountEtb(c.getRefundAmountEtb().value());
        e.setTypeSpecificData(c.getTypeSpecificData());
        e.setCurrentApprovalLevel(c.getCurrentApprovalLevel() != null
            ? c.getCurrentApprovalLevel().name() : null);
        e.setDraft(c.isDraft());
        if (c.getClosureDetails() != null) {
            CaseClosureDetails cd = c.getClosureDetails();
            e.setClosureReason(cd.closureReason().name());
            e.setClosureOutcome(cd.outcome() != null ? cd.outcome().name() : null);
            e.setClosureSummary(cd.resolutionSummary());
            e.setClosureAmountAssessed(cd.amountAssessed().value());
            e.setClosureAmountCollected(cd.amountCollected().value());
            e.setClosureAmountWrittenOff(cd.amountWrittenOff().value());
            e.setClosureRefundApproved(cd.refundApproved().value());
            e.setClosedById(cd.closedById());
            e.setClosedByName(cd.closedByName());
            e.setClosedAt(cd.closedAt());
            e.setClosureDocId(cd.finalDocumentId());
        }
        return e;
    }

    // ── child collection saves ────────────────────────────────────────────────

    private void saveNotes(List<CaseNote> notes) {
        notes.forEach(n -> {
            CaseNoteEntity e = new CaseNoteEntity();
            e.setId(n.getId()); e.setVersion(n.getVersion());
            e.setCaseId(n.getCaseId()); e.setContent(n.getContent());
            e.setOriginalContent(n.getOriginalContent());
            e.setEventDate(n.getEventDate()); e.setRecordingDate(n.getRecordingDate());
            e.setAuthorId(n.getAuthorId()); e.setAuthorName(n.getAuthorName());
            e.setAuthorRole(n.getAuthorRole().name());
            e.setObsolete(n.isObsolete()); e.setObsoleteReason(n.getObsoleteReason());
            e.setEdited(n.isEdited()); e.setAttachmentName(n.getAttachmentName());
            // Store editHistory as JSON via generic Map wrapper
            if (!n.getEditHistory().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    om.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    String json = om.writeValueAsString(n.getEditHistory());
                    e.setEditHistory(om.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {}));
                } catch (Exception ex) { /* ignore — history preserved in content */ }
            }
            CaseNoteEntity saved = noteRepo.save(e);
            n.setVersion(saved.getVersion());
        });
    }

    private void saveDocs(List<CaseDocument> docs) {
        docs.forEach(d -> {
            CaseDocumentEntity e = new CaseDocumentEntity();
            e.setId(d.getId()); e.setVersion(d.getVersion()); e.setCaseId(d.getCaseId());
            e.setName(d.getName()); e.setDocumentType(d.getDocumentType().name());
            e.setFileType(d.getFileType()); e.setSizeInMb(d.getSizeInMb());
            e.setUploadedById(d.getUploadedById()); e.setUploadedByName(d.getUploadedByName());
            e.setUploadedAt(d.getUploadedAt()); e.setClassification(d.getClassification().name());
            e.setDocVersion(d.getDocVersion()); e.setDmsReference(d.getDmsReference());
            CaseDocumentEntity saved = docRepo.save(e);
            d.setVersion(saved.getVersion());
        });
    }

    private void saveApprovals(List<ApprovalRecord> records) {
        records.forEach(r -> {
            if (!approvalRepo.existsById(r.getId())) {
                ApprovalRecordEntity e = new ApprovalRecordEntity();
                e.setId(r.getId()); e.setCaseId(r.getCaseId()); e.setLevel(r.getLevel().name());
                e.setApproverId(r.getApproverId()); e.setApproverName(r.getApproverName());
                e.setApproverRole(r.getApproverRole() != null ? r.getApproverRole().name() : null);
                e.setAction(r.getAction().name()); e.setComments(r.getComments());
                e.setOccurredAt(r.getOccurredAt());
                e.setNextLevel(r.getNextLevel() != null ? r.getNextLevel().name() : null);
                approvalRepo.save(e);
            }
        });
    }

    private void saveTasks(List<TaskReminder> tasks) {
        tasks.forEach(t -> {
            TaskReminderEntity e = new TaskReminderEntity();
            e.setId(t.getId()); e.setVersion(t.getVersion()); e.setCaseId(t.getCaseId());
            e.setCaseNumber(t.getCaseNumber()); e.setTitle(t.getTitle());
            e.setDescription(t.getDescription()); e.setDueDate(t.getDueDate());
            e.setDueTime(t.getDueTime()); e.setPriority(t.getPriority().name());
            e.setAssignedToUserId(t.getAssignedToUserId()); e.setAssignedToName(t.getAssignedToName());
            e.setRecipientType(t.getRecipientType() != null ? t.getRecipientType().name() : null);
            e.setStatus(t.getStatus().name());
            e.setReminderFrequency(t.getReminderFrequency().name());
            e.setLinkedActivityType(t.getLinkedActivityType() != null ? t.getLinkedActivityType().name() : null);
            e.setCancellationReason(t.getCancellationReason()); e.setCompletedAt(t.getCompletedAt());
            e.setCreatedAt(t.getCreatedAt());
            TaskReminderEntity saved = taskRepo.save(e);
            t.setVersion(saved.getVersion());
        });
    }

    // ── child toDomain helpers ────────────────────────────────────────────────

    private CaseNote noteToDomain(CaseNoteEntity e) {
        return CaseNote.reconstitute(
            e.getId(), e.getCaseId(), e.getOriginalContent(), e.getContent(),
            e.getEventDate(), e.getRecordingDate(),
            e.getAuthorId(), e.getAuthorName(), UserRole.valueOf(e.getAuthorRole()),
            e.isObsolete(), e.getObsoleteReason(), e.isEdited(),
            List.of(),   // editHistory restored separately if needed
            e.getAttachmentName(), e.getVersion()
        );
    }

    private CaseDocument docToDomain(CaseDocumentEntity e) {
        return CaseDocument.reconstitute(
            e.getId(), e.getCaseId(), e.getName(),
            CaseDocument.DocumentType.valueOf(e.getDocumentType()),
            e.getFileType(), e.getSizeInMb(), e.getUploadedById(), e.getUploadedByName(),
            e.getUploadedAt(), CaseDocument.SecurityClassification.valueOf(e.getClassification()),
            e.getDocVersion(), e.getDmsReference(), e.getVersion()
        );
    }

    private ApprovalRecord approvalToDomain(ApprovalRecordEntity e) {
        return ApprovalRecord.of(
            e.getId(), e.getCaseId(), ApprovalLevel.valueOf(e.getLevel()),
            e.getApproverId(), e.getApproverName(),
            e.getApproverRole() != null ? UserRole.valueOf(e.getApproverRole()) : null,
            com.act.casemanagement.domain.valueobject.ApprovalAction.valueOf(e.getAction()),
            e.getComments(), e.getOccurredAt(),
            e.getNextLevel() != null ? ApprovalLevel.valueOf(e.getNextLevel()) : ApprovalLevel.NONE
        );
    }

    private TaskReminder taskToDomain(TaskReminderEntity e) {
        return TaskReminder.reconstitute(
            e.getId(), e.getCaseId(), e.getCaseNumber(), e.getTitle(), e.getDescription(),
            e.getDueDate(), e.getDueTime(), Priority.valueOf(e.getPriority()),
            e.getAssignedToUserId(), e.getAssignedToName(),
            e.getRecipientType() != null ? TaskReminder.RecipientType.valueOf(e.getRecipientType()) : null,
            TaskReminder.Status.valueOf(e.getStatus()),
            TaskReminder.ReminderFrequency.valueOf(e.getReminderFrequency()),
            e.getLinkedActivityType() != null
                ? TaskReminder.LinkedActivityType.valueOf(e.getLinkedActivityType()) : null,
            e.getCancellationReason(), e.getCompletedAt(), e.getCreatedAt(), e.getVersion()
        );
    }
}
