package com.act.casemanagement.persistence.adapter;

import com.act.casemanagement.application.port.TaskReminderRepositoryPort;
import com.act.casemanagement.domain.model.TaskReminder;
import com.act.casemanagement.domain.valueobject.Priority;
import com.act.casemanagement.persistence.jpa.entity.TaskReminderEntity;
import com.act.casemanagement.persistence.jpa.repository.TaskReminderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskReminderPersistenceAdapter implements TaskReminderRepositoryPort {

    private final TaskReminderJpaRepository repo;

    @Override
    public List<TaskReminder> findDueOrOverdue(LocalDate asOf) {
        return repo.findDueOrOverdue(asOf).stream().map(this::toDomain).toList();
    }

    @Override
    public List<TaskReminder> findByCaseId(UUID caseId) {
        return repo.findByCaseId(caseId).stream().map(this::toDomain).toList();
    }

    @Override
    public TaskReminder save(TaskReminder reminder) {
        TaskReminderEntity e = toEntity(reminder);
        TaskReminderEntity saved = repo.save(e);
        reminder.setVersion(saved.getVersion());
        return reminder;
    }

    private TaskReminder toDomain(TaskReminderEntity e) {
        return TaskReminder.reconstitute(
            e.getId(), e.getCaseId(), e.getCaseNumber(), e.getTitle(), e.getDescription(),
            e.getDueDate(), e.getDueTime(), Priority.valueOf(e.getPriority()),
            e.getAssignedToUserId(), e.getAssignedToName(),
            e.getRecipientType() != null
                ? TaskReminder.RecipientType.valueOf(e.getRecipientType()) : null,
            TaskReminder.Status.valueOf(e.getStatus()),
            TaskReminder.ReminderFrequency.valueOf(e.getReminderFrequency()),
            e.getLinkedActivityType() != null
                ? TaskReminder.LinkedActivityType.valueOf(e.getLinkedActivityType()) : null,
            e.getCancellationReason(), e.getCompletedAt(), e.getCreatedAt(), e.getVersion()
        );
    }

    private TaskReminderEntity toEntity(TaskReminder t) {
        TaskReminderEntity e = new TaskReminderEntity();
        e.setId(t.getId()); e.setVersion(t.getVersion()); e.setCaseId(t.getCaseId());
        e.setCaseNumber(t.getCaseNumber()); e.setTitle(t.getTitle());
        e.setDescription(t.getDescription()); e.setDueDate(t.getDueDate());
        e.setDueTime(t.getDueTime()); e.setPriority(t.getPriority().name());
        e.setAssignedToUserId(t.getAssignedToUserId()); e.setAssignedToName(t.getAssignedToName());
        e.setRecipientType(t.getRecipientType() != null ? t.getRecipientType().name() : null);
        e.setStatus(t.getStatus().name());
        e.setReminderFrequency(t.getReminderFrequency().name());
        e.setLinkedActivityType(t.getLinkedActivityType() != null
            ? t.getLinkedActivityType().name() : null);
        e.setCancellationReason(t.getCancellationReason()); e.setCompletedAt(t.getCompletedAt());
        e.setCreatedAt(t.getCreatedAt() != null ? t.getCreatedAt() : java.time.Instant.now());
        return e;
    }
}
