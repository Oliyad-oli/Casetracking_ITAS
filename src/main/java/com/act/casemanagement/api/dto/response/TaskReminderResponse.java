package com.act.casemanagement.api.dto.response;

import com.act.casemanagement.domain.model.TaskReminder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskReminderResponse(
        UUID id, UUID caseId, String caseNumber, String title, String description,
        LocalDate dueDate, String dueTime, String priority,
        UUID assignedToUserId, String assignedToName,
        String status, String reminderFrequency, String linkedActivityType,
        String cancellationReason, Instant completedAt, Instant createdAt
) {
    public static TaskReminderResponse from(TaskReminder t) {
        return new TaskReminderResponse(t.getId(), t.getCaseId(), t.getCaseNumber(),
                t.getTitle(), t.getDescription(), t.getDueDate(), t.getDueTime(),
                t.getPriority().name(), t.getAssignedToUserId(), t.getAssignedToName(),
                t.getStatus().name(),
                t.getReminderFrequency().name(),
                t.getLinkedActivityType() != null ? t.getLinkedActivityType().name() : null,
                t.getCancellationReason(), t.getCompletedAt(), t.getCreatedAt());
    }
}
