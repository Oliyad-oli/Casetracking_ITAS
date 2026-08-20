package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.TaskReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskReminderJpaRepository extends JpaRepository<TaskReminderEntity, UUID> {
    List<TaskReminderEntity> findByCaseId(UUID caseId);

    @Query("SELECT t FROM TaskReminderEntity t WHERE t.status NOT IN ('COMPLETED','CANCELLED') " +
           "AND t.dueDate <= :asOf")
    List<TaskReminderEntity> findDueOrOverdue(@Param("asOf") LocalDate asOf);
}
