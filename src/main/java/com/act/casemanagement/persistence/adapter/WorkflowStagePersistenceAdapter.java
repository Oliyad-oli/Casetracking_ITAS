package com.act.casemanagement.persistence.adapter;

import com.act.casemanagement.application.port.WorkflowStageRepositoryPort;
import com.act.casemanagement.domain.aggregate.WorkflowStageConfig;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import com.act.casemanagement.domain.valueobject.UserRole;
import com.act.casemanagement.persistence.jpa.entity.WorkflowStageConfigEntity;
import com.act.casemanagement.persistence.jpa.repository.WorkflowStageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkflowStagePersistenceAdapter implements WorkflowStageRepositoryPort {

    private final WorkflowStageJpaRepository repo;

    @Override
    public List<WorkflowStageConfig> findAllActive() {
        return repo.findByActiveTrue().stream().map(this::toDomain).toList();
    }

    @Override
    public List<WorkflowStageConfig> findActiveByCategory(CaseCategory category) {
        return repo.findByActiveTrueAndCategory(category.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public WorkflowStageConfig save(WorkflowStageConfig config) {
        repo.save(toEntity(config)); return config;
    }

    @Override
    public void saveAll(List<WorkflowStageConfig> configs) {
        repo.saveAll(configs.stream().map(this::toEntity).toList());
    }

    private WorkflowStageConfig toDomain(WorkflowStageConfigEntity e) {
        List<UserRole> roles = e.getAllowedRoles().stream().map(UserRole::valueOf).toList();
        return WorkflowStageConfig.reconstitute(e.getId(), e.getVersion(), e.getName(),
            e.getCategory() != null ? CaseCategory.valueOf(e.getCategory()) : null,
            e.getDescription(), CaseStatus.valueOf(e.getStatus()), roles,
            e.getAllowedActions(), e.getSlaHours(),
            e.getNextStatus() != null ? CaseStatus.valueOf(e.getNextStatus()) : null,
            e.isActive(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private WorkflowStageConfigEntity toEntity(WorkflowStageConfig c) {
        WorkflowStageConfigEntity e = new WorkflowStageConfigEntity();
        e.setId(c.getId()); e.setVersion(c.getVersion()); e.setName(c.getName());
        e.setCategory(c.getCategory() != null ? c.getCategory().name() : null);
        e.setDescription(c.getDescription()); e.setStatus(c.getStatus().name());
        e.setAllowedRoles(c.getAllowedRoles().stream().map(Enum::name).toList());
        e.setAllowedActions(c.getAllowedActions());
        e.setSlaHours(c.getSlaHours());
        e.setNextStatus(c.getNextStatus() != null ? c.getNextStatus().name() : null);
        e.setActive(c.isActive());
        e.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt() : Instant.now());
        e.setUpdatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt() : Instant.now());
        return e;
    }
}
