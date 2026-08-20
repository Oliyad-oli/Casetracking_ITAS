package com.act.casemanagement.application.scheduling;

import com.act.casemanagement.application.port.NotificationEnginePort;
import com.act.casemanagement.application.port.OutboxPort;
import com.act.casemanagement.application.port.TaskReminderRepositoryPort;
import com.act.casemanagement.domain.aggregate.OutboxEntry;
import com.act.casemanagement.domain.model.TaskReminder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CTR0600 — Periodic repeat-alert mechanism for due/overdue task reminders.
 * Ports the exact frequency logic from AppContext.tsx scanForDueTaskAlerts():
 *   - ONCE:   alert once when due
 *   - DAILY:  alert every day while overdue
 *   - WEEKLY: alert every 7 days while overdue
 *
 * Alerts are dispatched through the outbox (never inline) so they survive
 * a notification-engine outage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskReminderScannerService {

    private final TaskReminderRepositoryPort reminderRepository;
    private final OutboxPort                 outboxPort;

    // In-memory last-alerted tracker — in production this would be a persistent table
    // to survive restarts. Replace with a DB-backed store when needed.
    private final ConcurrentHashMap<UUID, LocalDate> lastAlertedOn = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${task-reminder.scan-interval-ms:60000}")
    @Transactional
    public void scan() {
        LocalDate today = LocalDate.now();
        List<TaskReminder> due = reminderRepository.findDueOrOverdue(today);

        for (TaskReminder task : due) {
            if (!shouldAlert(task, today)) continue;

            lastAlertedOn.put(task.getId(), today);

            boolean isOverdue = task.getDueDate().isBefore(today);
            String title = isOverdue
                    ? "CRITICAL: Overdue Task — " + task.getCaseNumber()
                    : "Task Due Today — " + task.getCaseNumber();
            String message = "Task \"" + task.getTitle() + "\" for case "
                    + task.getCaseNumber() + (isOverdue
                    ? " is OVERDUE since " + task.getDueDate()
                    : " is due today at " + task.getDueTime());

            Map<String, Object> payload = new HashMap<>();
            payload.put("recipientId",    task.getAssignedToUserId().toString());
            payload.put("templateCode",   isOverdue ? "TASK_OVERDUE" : "TASK_DUE_TODAY");
            payload.put("taskId",         task.getId().toString());
            payload.put("caseNumber",     task.getCaseNumber());
            payload.put("title",          title);
            payload.put("message",        message);

            OutboxEntry entry = OutboxEntry.create(
                    UUID.randomUUID(), "CASE_NOTIFICATION", payload,
                    task.getCaseId(), "TaskReminder", 5);
            outboxPort.save(entry);

            log.info("TaskReminderScanner: queued alert taskId={} caseNumber={} overdue={}",
                    task.getId(), task.getCaseNumber(), isOverdue);
        }
    }

    private boolean shouldAlert(TaskReminder task, LocalDate today) {
        LocalDate last = lastAlertedOn.get(task.getId());
        if (last == null) return true;

        return switch (task.getReminderFrequency()) {
            case ONCE   -> false;  // already alerted once
            case DAILY  -> !last.equals(today);
            case WEEKLY -> last.plusDays(7).compareTo(today) <= 0;
        };
    }
}
