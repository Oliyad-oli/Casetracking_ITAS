package com.act.casemanagement.persistence.adapter;

import com.act.casemanagement.application.port.ApprovalMatrixRepositoryPort;
import com.act.casemanagement.domain.aggregate.ApprovalMatrixConfig;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.RiskLevel;
import com.act.casemanagement.persistence.jpa.entity.ApprovalMatrixConfigEntity;
import com.act.casemanagement.persistence.jpa.repository.ApprovalMatrixJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApprovalMatrixPersistenceAdapter implements ApprovalMatrixRepositoryPort {

    private final ApprovalMatrixJpaRepository repo;

    @Override
    public List<ApprovalMatrixConfig> findAllActive() {
        return repo.findByActiveTrue().stream().map(this::toDomain).toList();
    }

    @Override
    public ApprovalMatrixConfig save(ApprovalMatrixConfig config) {
        repo.save(toEntity(config));
        return config;
    }

    @Override
    public void saveAll(List<ApprovalMatrixConfig> configs) {
        repo.saveAll(configs.stream().map(this::toEntity).toList());
    }

    private ApprovalMatrixConfig toDomain(ApprovalMatrixConfigEntity e) {
        List<ApprovalLevel> levels = e.getRequiredApprovals().stream()
            .map(ApprovalLevel::valueOf).toList();
        CaseCategory caseType = "ALL".equals(e.getCaseType()) ? null
            : CaseCategory.valueOf(e.getCaseType());
        RiskLevel riskLevel = "ALL".equals(e.getRiskLevel()) ? null
            : RiskLevel.valueOf(e.getRiskLevel());
        return ApprovalMatrixConfig.reconstitute(e.getId(), e.getVersion(),
            caseType, e.getMinValueEtb(), e.getMaxValueEtb(),
            riskLevel, levels, e.isActive(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private ApprovalMatrixConfigEntity toEntity(ApprovalMatrixConfig c) {
        ApprovalMatrixConfigEntity e = new ApprovalMatrixConfigEntity();
        e.setId(c.getId()); e.setVersion(c.getVersion());
        e.setCaseType(c.getCaseType() != null ? c.getCaseType().name() : "ALL");
        e.setMinValueEtb(c.getMinValueEtb()); e.setMaxValueEtb(c.getMaxValueEtb());
        e.setRiskLevel(c.getRiskLevel() != null ? c.getRiskLevel().name() : "ALL");
        e.setRequiredApprovals(c.getRequiredApprovals().stream()
            .map(Enum::name).toList());
        e.setActive(c.isActive());
        e.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt() : Instant.now());
        e.setUpdatedAt(Instant.now());
        return e;
    }
}
