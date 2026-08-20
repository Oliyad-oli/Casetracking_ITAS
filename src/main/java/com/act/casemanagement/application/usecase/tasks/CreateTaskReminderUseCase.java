package com.act.casemanagement.application.usecase.tasks;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.application.port.EventPublisherPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.model.TaskReminder;
import com.act.casemanagement.domain.valueobject.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTaskReminderUseCase {

    private final CaseRepositoryPort caseRepository;
    private final EventPublisherPort eventPublisher;

    @Transactional
    public TaskReminder execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        TaskReminder task = aCase.addTaskReminder(
                UUID.randomUUID(),
                cmd.title(),
                cmd.description(),
                cmd.dueDate(),
                cmd.dueTime(),
                cmd.priority(),
                cmd.assignedToUserId() != null ? cmd.assignedToUserId() : actor.getActorId(),
                cmd.assignedToName(),
                cmd.recipientType(),
                cmd.reminderFrequency(),
                cmd.linkedActivityType()
        );

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);

        return saved.getTaskReminders().stream()
                .filter(t -> t.getId().equals(task.getId()))
                .findFirst()
                .orElse(task);
    }

    public record Command(
            UUID caseId,
            String title,
            String description,
            LocalDate dueDate,
            String dueTime,
            Priority priority,
            UUID assignedToUserId,
            String assignedToName,
            TaskReminder.RecipientType recipientType,
            TaskReminder.ReminderFrequency reminderFrequency,
            TaskReminder.LinkedActivityType linkedActivityType
    ) {}
}
