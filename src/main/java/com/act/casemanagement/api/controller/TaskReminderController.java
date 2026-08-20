package com.act.casemanagement.api.controller;

import com.act.casemanagement.api.dto.request.CreateTaskReminderRequest;
import com.act.casemanagement.api.dto.response.TaskReminderResponse;
import com.act.casemanagement.application.usecase.tasks.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** CTR0600 — Task reminders and repeat-alert scheduling. */
@RestController
@RequestMapping("/cases/{caseId}/tasks")
@Tag(name = "CTR0600", description = "Task Reminders (CTR0600)")
@RequiredArgsConstructor
public class TaskReminderController {

    private final CreateTaskReminderUseCase createTaskUseCase;
    private final CompleteTaskUseCase       completeTaskUseCase;
    private final CancelTaskReminderUseCase cancelTaskUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a task reminder")
    public TaskReminderResponse create(@PathVariable UUID caseId,
            @Valid @RequestBody CreateTaskReminderRequest req) {
        return TaskReminderResponse.from(createTaskUseCase.execute(req.toCommand(caseId)));
    }

    @PostMapping("/{taskId}/complete")
    @Operation(summary = "Mark a task as completed")
    public void complete(@PathVariable UUID caseId, @PathVariable UUID taskId) {
        completeTaskUseCase.execute(caseId, taskId);
    }

    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "Cancel a task reminder")
    public void cancel(@PathVariable UUID caseId, @PathVariable UUID taskId,
            @RequestParam String reason) {
        cancelTaskUseCase.execute(caseId, taskId, reason);
    }
}
