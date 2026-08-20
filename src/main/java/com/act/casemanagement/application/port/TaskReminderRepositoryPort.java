package com.act.casemanagement.application.port;

import com.act.casemanagement.domain.model.TaskReminder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskReminderRepositoryPort {
    /** All non-completed, non-cancelled reminders due on or before today — for the repeat-alert scanner. */
    List<TaskReminder> findDueOrOverdue(LocalDate asOf);
    List<TaskReminder> findByCaseId(UUID caseId);
    TaskReminder save(TaskReminder reminder);
}
