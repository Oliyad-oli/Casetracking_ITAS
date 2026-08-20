package com.act.casemanagement.api.dto.request;

import com.act.casemanagement.application.usecase.tasks.CreateTaskReminderUseCase;
import com.act.casemanagement.domain.model.TaskReminder;
import com.act.casemanagement.domain.valueobject.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskReminderRequest(
        @NotBlank String title,
                  String description,
        @NotNull  LocalDate dueDate,
                  String dueTime,
        @NotNull  Priority priority,
                  UUID assignedToUserId,
                  String assignedToName,
                  TaskReminder.RecipientType recipientType,
                  TaskReminder.ReminderFrequency reminderFrequency,
                  TaskReminder.LinkedActivityType linkedActivityType
) {
    public CreateTaskReminderUseCase.Command toCommand(UUID caseId) {
        return new CreateTaskReminderUseCase.Command(caseId, title, description, dueDate,
                dueTime != null ? dueTime : "17:00", priority, assignedToUserId, assignedToName,
                recipientType != null ? recipientType : TaskReminder.RecipientType.OFFICER,
                reminderFrequency != null ? reminderFrequency : TaskReminder.ReminderFrequency.ONCE,
                linkedActivityType != null ? linkedActivityType : TaskReminder.LinkedActivityType.INTERNAL_REVIEW);
    }
}
