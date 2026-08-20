package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.WorkflowStageConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowStageJpaRepository extends JpaRepository<WorkflowStageConfigEntity, UUID> {
    List<WorkflowStageConfigEntity> findByActiveTrue();
    List<WorkflowStageConfigEntity> findByActiveTrueAndCategory(String category);
}
