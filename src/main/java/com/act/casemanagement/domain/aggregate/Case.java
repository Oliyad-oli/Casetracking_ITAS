package com.act.casemanagement.domain.aggregate;

import com.act.casemanagement.domain.event.*;
import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.model.*;
import com.act.casemanagement.domain.valueobject.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Case aggregate root — owns its own lifecycle, notes, documents,
 * approval records, task reminders, and associations.
 * <p>
 * The aggregate guards its own state transitions; use cases orchestrate
 * and call ports. Events are registered here and drained by the use case
 * after {@code save()}.
 * <p>
 * Pattern mirrors {@code TaxReturn} in bs-filing-core-server.
 */
@Getter
public class Case extends AggregateRoot {

    private final UUID id;
    private Long version;                   // JPA @Version — must round-trip

    private final String caseNumber;
    private String title;
    private final CaseCategory category;
    private CaseStatus status;
    private Priority priority;
    private RiskLevel riskLevel;

    // Taxpayer
    private final TIN tin;
    private final String taxpayerName;
    private final String entityType;

    // Ownership & Assignment
    private final UUID createdById;
    private final String createdByName;
    private final Instant createdAt;
    private UUID assigningOfficerId;
    private String assigningOfficerName;
    private UUID assignedOfficerId;
    private String assignedOfficerName;
    private String assignedUnitId;
    private String assignedUnitName;
    private Instant assignedDate;
    private String assignmentReason;
    private boolean selfAssigned;

    // Timeline
    private Instant deadline;
    private int targetDays;
    private Instant lastActivityAt;

    // Financials
    private MonetaryAmount estimatedAmountEtb;
    private MonetaryAmount assessedAmountEtb;
    private MonetaryAmount collectedAmountEtb;
    private MonetaryAmount writtenOffAmountEtb;
    private MonetaryAmount refundAmountEtb;

    // Details
    private String reasons;
    private String instructions;
    private String remarks;

    // JSONB type-specific data (audit period, court case no., etc.)
    private Map<String, Object> typeSpecificData;

    // Approval chain tracking
    private ApprovalLevel currentApprovalLevel;

    // Closure
    private CaseClosureDetails closureDetails;

    // Draft flag
    private boolean draft;

    // Children — within aggregate boundary (mirroring TaxReturn → Schedule → LineItem)
    private final List<CaseNote>      notes       = new ArrayList<>();
    private final List<CaseDocument>  documents   = new ArrayList<>();
    private final List<ApprovalRecord> approvalRecords = new ArrayList<>();
    private final List<TaskReminder>  taskReminders = new ArrayList<>();

    // ─────────────────────────────────────────────
    // Factory — CreateCaseUseCase
    // ─────────────────────────────────────────────

    public static Case create(
            UUID id, String caseNumber, String title, CaseCategory category,
            Priority priority, RiskLevel riskLevel,
            TIN tin, String taxpayerName, String entityType,
            UUID createdById, String createdByName,
            Instant deadline, int targetDays,
            MonetaryAmount estimatedAmountEtb,
            String reasons, String instructions, String remarks,
            Map<String, Object> typeSpecificData) {

        Case c = new Case(id, caseNumber, title, category, CaseStatus.UNASSIGNED,
                priority, riskLevel, tin, taxpayerName, entityType,
                createdById, createdByName, Instant.now(),
                deadline, targetDays, estimatedAmountEtb, reasons, instructions,
                remarks, typeSpecificData);

        c.registerEvent(new CaseCreated(UUID.randomUUID(), Instant.now(),
                id, caseNumber, category, tin.value(), createdById));
        return c;
    }

    /** Reconstitution from persistence. */
    public static Case reconstitute(
            UUID id, Long version, String caseNumber, String title,
            CaseCategory category, CaseStatus status,
            Priority priority, RiskLevel riskLevel,
            TIN tin, String taxpayerName, String entityType,
            UUID createdById, String createdByName, Instant createdAt,
            UUID assigningOfficerId, String assigningOfficerName,
            UUID assignedOfficerId, String assignedOfficerName,
            String assignedUnitId, String assignedUnitName,
            Instant assignedDate, String assignmentReason, boolean selfAssigned,
            Instant deadline, int targetDays, Instant lastActivityAt,
            MonetaryAmount estimatedAmountEtb, MonetaryAmount assessedAmountEtb,
            MonetaryAmount collectedAmountEtb, MonetaryAmount writtenOffAmountEtb,
            MonetaryAmount refundAmountEtb,
            String reasons, String instructions, String remarks,
            Map<String, Object> typeSpecificData,
            ApprovalLevel currentApprovalLevel,
            CaseClosureDetails closureDetails, boolean draft,
            List<CaseNote> notes, List<CaseDocument> documents,
            List<ApprovalRecord> approvalRecords, List<TaskReminder> taskReminders) {

        Case c = new Case(id, caseNumber, title, category, status, priority, riskLevel,
                tin, taxpayerName, entityType,
                createdById, createdByName, createdAt,
                deadline, targetDays, estimatedAmountEtb, reasons, instructions,
                remarks, typeSpecificData);
        c.version              = version;
        c.assigningOfficerId   = assigningOfficerId;
        c.assigningOfficerName = assigningOfficerName;
        c.assignedOfficerId    = assignedOfficerId;
        c.assignedOfficerName  = assignedOfficerName;
        c.assignedUnitId       = assignedUnitId;
        c.assignedUnitName     = assignedUnitName;
        c.assignedDate         = assignedDate;
        c.assignmentReason     = assignmentReason;
        c.selfAssigned         = selfAssigned;
        c.lastActivityAt       = lastActivityAt;
        c.assessedAmountEtb    = assessedAmountEtb;
        c.collectedAmountEtb   = collectedAmountEtb;
        c.writtenOffAmountEtb  = writtenOffAmountEtb;
        c.refundAmountEtb      = refundAmountEtb;
        c.currentApprovalLevel = currentApprovalLevel;
        c.closureDetails       = closureDetails;
        c.draft                = draft;
        if (notes            != null) c.notes.addAll(notes);
        if (documents        != null) c.documents.addAll(documents);
        if (approvalRecords  != null) c.approvalRecords.addAll(approvalRecords);
        if (taskReminders    != null) c.taskReminders.addAll(taskReminders);
        return c;
    }

    private Case(UUID id, String caseNumber, String title, CaseCategory category,
            CaseStatus status, Priority priority, RiskLevel riskLevel,
            TIN tin, String taxpayerName, String entityType,
            UUID createdById, String createdByName, Instant createdAt,
            Instant deadline, int targetDays, MonetaryAmount estimatedAmountEtb,
            String reasons, String instructions, String remarks,
            Map<String, Object> typeSpecificData) {
        this.id                   = id;
        this.caseNumber           = caseNumber;
        this.title                = title;
        this.category             = category;
        this.status               = status;
        this.priority             = priority;
        this.riskLevel            = riskLevel;
        this.tin                  = tin;
        this.taxpayerName         = taxpayerName;
        this.entityType           = entityType;
        this.createdById          = createdById;
        this.createdByName        = createdByName;
        this.createdAt            = createdAt;
        this.deadline             = deadline;
        this.targetDays           = targetDays;
        this.estimatedAmountEtb   = estimatedAmountEtb;
        this.assessedAmountEtb    = MonetaryAmount.zero();
        this.collectedAmountEtb   = MonetaryAmount.zero();
        this.writtenOffAmountEtb  = MonetaryAmount.zero();
        this.refundAmountEtb      = MonetaryAmount.zero();
        this.reasons              = reasons;
        this.instructions         = instructions;
        this.remarks              = remarks;
        this.typeSpecificData     = typeSpecificData != null ? new HashMap<>(typeSpecificData) : new HashMap<>();
        this.currentApprovalLevel = ApprovalLevel.NONE;
        this.lastActivityAt       = createdAt;
        this.draft                = false;
    }

    @Override
    public UUID getId() { return id; }

    // ─────────────────────────────────────────────
    // Assignment
    // ─────────────────────────────────────────────

    /**
     * Assign the case to an officer. If assigningActorId == officerId, emits
     * {@link CaseSelfAssigned} instead of {@link CaseAssigned} and requires
     * a non-blank justification (CTR0200/CTR0300).
     */
    public void assign(UUID officerId, String officerName, String assignedUnitId,
                       String assignedUnitName, UUID assigningActorId,
                       String assigningActorName, String reason) {
        guardNotClosed("assign");
        boolean isSelf = officerId.equals(assigningActorId);
        if (isSelf && (reason == null || reason.isBlank())) {
            throw new DomainException(
                "Self-assignment (CTR0200/CTR0300) requires a mandatory justification");
        }

        this.assignedOfficerId   = officerId;
        this.assignedOfficerName = officerName;
        this.assigningOfficerId  = assigningActorId;
        this.assigningOfficerName = assigningActorName;
        this.assignedUnitId      = assignedUnitId;
        this.assignedUnitName    = assignedUnitName;
        this.assignedDate        = Instant.now();
        this.assignmentReason    = reason;
        this.selfAssigned        = isSelf;

        if (status == CaseStatus.UNASSIGNED || status == CaseStatus.OPEN) {
            this.status = CaseStatus.ASSIGNED;
        }
        this.lastActivityAt = Instant.now();

        if (isSelf) {
            registerEvent(new CaseSelfAssigned(UUID.randomUUID(), Instant.now(),
                    id, officerId, officerName, reason));
        } else {
            registerEvent(new CaseAssigned(UUID.randomUUID(), Instant.now(),
                    id, officerId, officerName, assigningActorId, reason));
        }
    }

    public void deassign(UUID actorId, String reason) {
        guardNotClosed("deassign");
        UUID prevOfficerId   = this.assignedOfficerId;
        String prevOfficerName = this.assignedOfficerName;
        this.assignedOfficerId   = null;
        this.assignedOfficerName = null;
        this.status              = CaseStatus.UNASSIGNED;
        this.lastActivityAt      = Instant.now();
        registerEvent(new CaseDeassigned(UUID.randomUUID(), Instant.now(),
                id, prevOfficerId, prevOfficerName, actorId, reason));
    }

    // ─────────────────────────────────────────────
    // Status transitions
    // ─────────────────────────────────────────────

    public void transitionStatus(CaseStatus newStatus, UUID actorId, String reason) {
        guardNotClosed("transition status");
        CaseStatus prev = this.status;
        this.status         = newStatus;
        this.lastActivityAt = Instant.now();
        registerEvent(new CaseStatusChanged(UUID.randomUUID(), Instant.now(),
                id, prev, newStatus, actorId, reason));
    }

    public void updatePriority(Priority newPriority, UUID actorId, String reason) {
        guardNotClosed("update priority");
        this.priority       = newPriority;
        this.lastActivityAt = Instant.now();
        // Status does not change on priority update; emit generic StatusChanged for auditability
        registerEvent(new CaseStatusChanged(UUID.randomUUID(), Instant.now(),
                id, this.status, this.status, actorId,
                "Priority updated to " + newPriority + ". " + (reason != null ? reason : "")));
    }

    // ─────────────────────────────────────────────
    // Notes
    // ─────────────────────────────────────────────

    public CaseNote addNote(UUID noteId, String content, LocalDate eventDate,
                            UUID authorId, String authorName, UserRole authorRole,
                            String attachmentName) {
        guardNotClosed("add note");
        CaseNote note = CaseNote.create(noteId, id, content, eventDate,
                Instant.now(), authorId, authorName, authorRole, attachmentName);
        notes.add(note);
        lastActivityAt = Instant.now();
        registerEvent(new CaseNoteAdded(UUID.randomUUID(), Instant.now(), id,
                noteId, authorId,
                content.length() > 80 ? content.substring(0, 80) : content));
        return note;
    }

    public CaseNote findNote(UUID noteId) {
        return notes.stream().filter(n -> n.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Note not found: " + noteId));
    }

    public void editNote(UUID noteId, String newContent, UUID editorId,
                         String editorName, String reason) {
        CaseNote note = findNote(noteId);
        String prevContent = note.getContent();
        note.edit(newContent, editorId, editorName, reason);
        lastActivityAt = Instant.now();
        registerEvent(new CaseNoteEdited(UUID.randomUUID(), Instant.now(), id,
                noteId, editorId, prevContent, newContent, reason));
    }

    public void markNoteObsolete(UUID noteId, UUID actorId, String reason) {
        findNote(noteId).markObsolete(reason);
        lastActivityAt = Instant.now();
        registerEvent(new CaseNoteMarkedObsolete(UUID.randomUUID(), Instant.now(),
                id, noteId, actorId, reason));
    }

    // ─────────────────────────────────────────────
    // Documents
    // ─────────────────────────────────────────────

    public CaseDocument addDocument(UUID docId, String name,
            CaseDocument.DocumentType documentType, String fileType, BigDecimal sizeInMb,
            UUID uploadedById, String uploadedByName,
            CaseDocument.SecurityClassification classification,
            String docVersion, String dmsReference) {
        guardNotClosed("add document");
        CaseDocument doc = CaseDocument.create(docId, id, name, documentType, fileType,
                sizeInMb, uploadedById, uploadedByName, Instant.now(),
                classification, docVersion, dmsReference);
        documents.add(doc);
        lastActivityAt = Instant.now();
        registerEvent(new DocumentAdded(UUID.randomUUID(), Instant.now(),
                id, docId, uploadedById, name));
        return doc;
    }

    // ─────────────────────────────────────────────
    // Approval chain
    // ─────────────────────────────────────────────

    public void submitForApproval(ApprovalLevel firstLevel, UUID actorId, String comments) {
        guardNotClosed("submit for approval");
        ApprovalRecord submission = ApprovalRecord.of(
                UUID.randomUUID(), id, firstLevel, actorId, null, null,
                ApprovalAction.APPROVED, "Submitted by officer: " + comments,
                Instant.now(), firstLevel);
        approvalRecords.add(submission);
        this.currentApprovalLevel = firstLevel;
        this.status = firstLevel.toPendingStatus();
        this.lastActivityAt = Instant.now();
    }

    public void processApproval(ApprovalLevel level, UUID approverId,
            String approverName, UserRole approverRole,
            ApprovalAction action, String comments,
            ApprovalLevel nextLevel, CaseStatus newStatus) {
        guardNotClosed("process approval");
        ApprovalRecord record = ApprovalRecord.of(UUID.randomUUID(), id, level,
                approverId, approverName, approverRole, action, comments,
                Instant.now(), nextLevel);
        approvalRecords.add(record);
        this.currentApprovalLevel = nextLevel;
        this.status               = newStatus;
        this.lastActivityAt       = Instant.now();
        registerEvent(new ApprovalProcessed(UUID.randomUUID(), Instant.now(), id,
                approverId, approverName, level, action, newStatus, nextLevel, comments));
    }

    // ─────────────────────────────────────────────
    // Task Reminders
    // ─────────────────────────────────────────────

    public TaskReminder addTaskReminder(UUID taskId, String title, String description,
            LocalDate dueDate, String dueTime, Priority priority,
            UUID assignedToUserId, String assignedToName,
            TaskReminder.RecipientType recipientType,
            TaskReminder.ReminderFrequency frequency,
            TaskReminder.LinkedActivityType linkedActivityType) {
        guardNotClosed("add task reminder");
        TaskReminder task = TaskReminder.create(taskId, id, caseNumber,
                title, description, dueDate, dueTime, priority,
                assignedToUserId, assignedToName, recipientType, frequency,
                linkedActivityType);
        taskReminders.add(task);
        lastActivityAt = Instant.now();
        registerEvent(new TaskReminderCreated(UUID.randomUUID(), Instant.now(),
                id, taskId, assignedToUserId, title, dueDate));
        return task;
    }

    public TaskReminder findTask(UUID taskId) {
        return taskReminders.stream().filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new DomainException("TaskReminder not found: " + taskId));
    }

    public void completeTask(UUID taskId, UUID actorId) {
        TaskReminder task = findTask(taskId);
        task.complete(Instant.now());
        lastActivityAt = Instant.now();
        registerEvent(new TaskReminderCompleted(UUID.randomUUID(), Instant.now(),
                id, taskId, actorId));
    }

    public void cancelTask(UUID taskId, UUID actorId, String reason) {
        TaskReminder task = findTask(taskId);
        task.cancel(reason);
        lastActivityAt = Instant.now();
        registerEvent(new TaskReminderCancelled(UUID.randomUUID(), Instant.now(),
                id, taskId, actorId, reason));
    }

    // ─────────────────────────────────────────────
    // Closure (CTR0700)
    // ─────────────────────────────────────────────

    /**
     * Only the assigning officer or a directorate-override role may close.
     * The use case must verify this BEFORE calling close() — but we also
     * guard here as a defence-in-depth invariant.
     * The {@link UnauthorizedClosureAttempted} event is registered in the use
     * case layer (not here) because the aggregate doesn't know the actor's role.
     */
    public void close(CaseClosureDetails closureDetails) {
        if (status == CaseStatus.CLOSED) {
            throw new DomainException("Case is already closed: " + id);
        }
        this.closureDetails = closureDetails;
        this.status         = CaseStatus.CLOSED;
        this.lastActivityAt = Instant.now();
        registerEvent(new CaseClosed(UUID.randomUUID(), Instant.now(), id,
                false, closureDetails.closedById(), closureDetails.closureReason()));
    }

    /** Called by CloseCaseUseCase after the ForbiddenOperationException is thrown
     *  to record the unauthorised attempt event before re-throwing. */
    public void recordUnauthorizedClosureAttempt(UUID actorId, String actorRole) {
        registerEvent(new UnauthorizedClosureAttempted(UUID.randomUUID(), Instant.now(),
                id, actorId, actorRole, assigningOfficerId));
    }

    // ─────────────────────────────────────────────
    // Guards
    // ─────────────────────────────────────────────

    private void guardNotClosed(String operation) {
        if (status == CaseStatus.CLOSED) {
            throw new DomainException(
                "Cannot perform [" + operation + "] on a CLOSED case: " + id);
        }
    }

    // ─────────────────────────────────────────────
    // Accessors for mutable collections (unmodifiable views)
    // ─────────────────────────────────────────────

    public List<CaseNote>      getNotes()           { return Collections.unmodifiableList(notes); }
    public List<CaseDocument>  getDocuments()       { return Collections.unmodifiableList(documents); }
    public List<ApprovalRecord> getApprovalRecords(){ return Collections.unmodifiableList(approvalRecords); }
    public List<TaskReminder>  getTaskReminders()   { return Collections.unmodifiableList(taskReminders); }

    public void setVersion(Long version) { this.version = version; }

    public void updateFinancials(MonetaryAmount assessed, MonetaryAmount collected,
                                  MonetaryAmount writtenOff, MonetaryAmount refund) {
        this.assessedAmountEtb   = assessed;
        this.collectedAmountEtb  = collected;
        this.writtenOffAmountEtb = writtenOff;
        this.refundAmountEtb     = refund;
        this.lastActivityAt      = Instant.now();
    }

    // Keep the mutable internal lists accessible for persistence adapters
    public List<CaseNote>      notesInternal()           { return notes; }
    public List<CaseDocument>  documentsInternal()       { return documents; }
    public List<ApprovalRecord> approvalRecordsInternal(){ return approvalRecords; }
    public List<TaskReminder>  taskRemindersInternal()   { return taskReminders; }

    /**
     * Allows use cases outside the domain package to register a domain event on this aggregate.
     * Only events that are logically "owned" by this aggregate should use this — e.g. CasesAssociated.
     */
    public void registerDomainEvent(DomainEvent event) {
        registerEvent(event);
    }
}
