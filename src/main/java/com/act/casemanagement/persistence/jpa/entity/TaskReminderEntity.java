package com.act.casemanagement.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "task_reminders")
@Getter @Setter
public class TaskReminderEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "case_number", length = 60)
    private String caseNumber;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "due_time", length = 10)
    private String dueTime;

    @Column(nullable = false, length = 20)
    private String priority;

    @Column(name = "assigned_to_user_id", nullable = false)
    private UUID assignedToUserId;

    @Column(name = "assigned_to_name", length = 200)
    private String assignedToName;

    @Column(name = "recipient_type", length = 30)
    private String recipientType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "reminder_frequency", nullable = false, length = 20)
    private String reminderFrequency;

    @Column(name = "linked_activity_type", length = 60)
    private String linkedActivityType;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
