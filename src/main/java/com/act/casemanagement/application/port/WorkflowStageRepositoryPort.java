package com.act.casemanagement.application.port;

import com.act.casemanagement.domain.aggregate.WorkflowStageConfig;
import com.act.casemanagement.domain.valueobject.CaseCategory;

import java.util.List;

public interface WorkflowStageRepositoryPort {
    List<WorkflowStageConfig> findAllActive();
    List<WorkflowStageConfig> findActiveByCategory(CaseCategory category);
    WorkflowStageConfig save(WorkflowStageConfig config);
    void saveAll(List<WorkflowStageConfig> configs);
}
