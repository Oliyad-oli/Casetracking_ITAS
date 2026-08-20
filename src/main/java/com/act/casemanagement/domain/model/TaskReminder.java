package com.act.casemanagement.domain.model;

import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.valueobject.Priority;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Task/diary reminder — child entity of the Case aggregate. */
@Getter
public class TaskReminder {

    public enum Status { PENDING, DUE_TODAY, OVERDUE, COMPLETED, CANCELLED }
    public enum ReminderFrequency { ONCE, DAILY, WEEKLY }
    public enum LinkedActivityType {
        TAXPAYER_RESPONSE_DEADLINE, INTERNAL_REVIEW, STATUTORY_DUE_DATE,
        COURT_HEARING, FIELD_AUDIT
    }
    public enum RecipientType { OFFICER, SUPERVISOR, TAXPAYER, TAX_AGENT }

    private final UUID id;
    private final UUID caseId;
    private final String caseNumber;
    private final String title;
    private final String description;
    private final LocalDate dueDate;
    private final String dueTime;
    private final Priority priority;
    private final UUID assignedToUserId;
    private final String assignedToName;
    private final RecipientType recipientType;
    private Status status;
    private final ReminderFrequency reminderFrequency;
    private final LinkedActivityType linkedActivityType;
    private String cancellationReason;
    private Instant completedAt;
    private final Instant createdAt;
    private Long version;

    public static TaskReminder create(UUID id, UUID caseId, String caseNumber,
            String title, String description, LocalDate dueDate, String dueTime,
            Priority priority, UUID assignedToUserId, String assignedToName,
            RecipientType recipientType, ReminderFrequency reminderFrequency,
            LinkedActivityType linkedActivityType) {
        return new TaskReminder(id, caseId, caseNumber, title, description, dueDate,
                dueTime, priority, assignedToUserId, assignedToName, recipientType,
                Status.PENDING, reminderFrequency, linkedActivityType, null, null,
                Instant.now(), null);
    }

    public static TaskReminder reconstitute(UUID id, UUID caseId, String caseNumber,
            String title, String description, LocalDate dueDate, String dueTime,
            Priority priority, UUID assignedToUserId, String assignedToName,
            RecipientType recipientType, Status status, ReminderFrequency reminderFrequency,
            LinkedActivityType linkedActivityType, String cancellationReason,
            Instant completedAt, Instant createdAt, Long version) {
        return new TaskReminder(id, caseId, caseNumber, title, description, dueDate,
                dueTime, priority, assignedToUserId, assignedToName, recipientType,
                status, reminderFrequency, linkedActivityType, cancellationReason,
                completedAt, createdAt, version);
    }

    private TaskReminder(UUID id, UUID caseId, String caseNumber, String title,
            String description, LocalDate dueDate, String dueTime, Priority priority,
            UUID assignedToUserId, String assignedToName, RecipientType recipientType,
            Status status, ReminderFrequency reminderFrequency,
            LinkedActivityType linkedActivityType, String cancellationReason,
            Instant completedAt, Instant createdAt, Long version) {
        this.id                  = id;
        this.caseId              = caseId;
        this.caseNumber          = caseNumber;
        this.title               = title;
        this.description         = description;
        this.dueDate             = dueDate;
        this.dueTime             = dueTime;
        this.priority            = priority;
        this.assignedToUserId    = assignedToUserId;
        this.assignedToName      = assignedToName;
        this.recipientType       = recipientType;
        this.status              = status;
        this.reminderFrequency   = reminderFrequency;
        this.linkedActivityType  = linkedActivityType;
        this.cancellationReason  = cancellationReason;
        this.completedAt         = completedAt;
        this.createdAt           = createdAt;
        this.version             = version;
    }

    public void complete(Instant at) {
        if (status == Status.CANCELLED || status == Status.COMPLETED) {
            throw new DomainException("Cannot complete task in status " + status);
        }
        this.status      = Status.COMPLETED;
        this.completedAt = at;
    }

    public void cancel(String reason) {
        if (status == Status.COMPLETED) {
            throw new DomainException("Cannot cancel a completed task");
        }
        this.status             = Status.CANCELLED;
        this.cancellationReason = reason;
    }

    public void setVersion(Long version) { this.version = version; }
}
