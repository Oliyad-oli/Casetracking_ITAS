package com.act.casemanagement.persistence.adapter;

import com.act.casemanagement.application.port.CaseTypeConfigRepositoryPort;
import com.act.casemanagement.domain.aggregate.CaseTypeConfig;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import com.act.casemanagement.persistence.jpa.entity.CaseTypeConfigEntity;
import com.act.casemanagement.persistence.jpa.repository.CaseTypeConfigJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CaseTypeConfigPersistenceAdapter implements CaseTypeConfigRepositoryPort {

    private final CaseTypeConfigJpaRepository repo;

    @Override
    public Optional<CaseTypeConfig> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<CaseTypeConfig> findByCode(CaseCategory code) {
        return repo.findByCode(code.name()).map(this::toDomain);
    }

    @Override
    public List<CaseTypeConfig> findAllActive() {
        return repo.findByActiveTrue().stream().map(this::toDomain).toList();
    }

    @Override
    public CaseTypeConfig save(CaseTypeConfig config) {
        repo.save(toEntity(config)); return config;
    }

    private CaseTypeConfig toDomain(CaseTypeConfigEntity e) {
        List<CaseStatus>    statuses = e.getAllowedStatuses().stream()
            .map(CaseStatus::valueOf).toList();
        List<ApprovalLevel> levels   = e.getApprovalLevels().stream()
            .map(ApprovalLevel::valueOf).toList();
        return CaseTypeConfig.reconstitute(e.getId(), e.getVersion(),
            CaseCategory.valueOf(e.getCode()), e.getName(), e.getDescription(),
            e.isActive(), e.getEffectiveDate(), e.getConfigVersion(),
            e.getDefaultTargetDays(), e.getFieldsDefinition(), statuses, levels,
            e.getCreatedAt(), e.getUpdatedAt());
    }

    private CaseTypeConfigEntity toEntity(CaseTypeConfig c) {
        CaseTypeConfigEntity e = new CaseTypeConfigEntity();
        e.setId(c.getId()); e.setVersion(c.getVersion());
        e.setCode(c.getCode().name()); e.setName(c.getName());
        e.setDescription(c.getDescription()); e.setActive(c.isActive());
        e.setEffectiveDate(c.getEffectiveDate()); e.setConfigVersion(c.getConfigVersion());
        e.setDefaultTargetDays(c.getDefaultTargetDays());
        e.setFieldsDefinition(c.getFieldsDefinition());
        e.setAllowedStatuses(c.getAllowedStatuses() != null
            ? c.getAllowedStatuses().stream().map(Enum::name).toList() : List.of());
        e.setApprovalLevels(c.getApprovalLevels() != null
            ? c.getApprovalLevels().stream().map(Enum::name).toList() : List.of());
        e.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt() : Instant.now());
        e.setUpdatedAt(Instant.now());
        return e;
    }
}
