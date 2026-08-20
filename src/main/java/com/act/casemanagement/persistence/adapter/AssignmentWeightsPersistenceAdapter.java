package com.act.casemanagement.persistence.adapter;

import com.act.casemanagement.application.port.AssignmentWeightsRepositoryPort;
import com.act.casemanagement.domain.aggregate.AssignmentWeightsConfig;
import com.act.casemanagement.persistence.jpa.entity.AssignmentWeightsConfigEntity;
import com.act.casemanagement.persistence.jpa.repository.AssignmentWeightsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AssignmentWeightsPersistenceAdapter implements AssignmentWeightsRepositoryPort {

    private final AssignmentWeightsJpaRepository repo;

    @Override
    public Optional<AssignmentWeightsConfig> findActive() {
        return repo.findFirstByActiveTrueOrderByEffectiveFromDesc().map(this::toDomain);
    }

    @Override
    public AssignmentWeightsConfig save(AssignmentWeightsConfig config) {
        AssignmentWeightsConfigEntity e = new AssignmentWeightsConfigEntity();
        e.setId(config.getId()); e.setVersion(config.getVersion());
        e.setCapacityWeight(config.getCapacityWeight());
        e.setSpecializationWeight(config.getSpecializationWeight());
        e.setRegionWeight(config.getRegionWeight());
        e.setCaseloadMixWeight(config.getCaseloadMixWeight());
        e.setActive(config.isActive());
        e.setEffectiveFrom(config.getEffectiveFrom());
        e.setCreatedById(config.getCreatedById());
        e.setCreatedAt(config.getCreatedAt());
        AssignmentWeightsConfigEntity saved = repo.save(e);
        config.setVersion(saved.getVersion());
        return config;
    }

    private AssignmentWeightsConfig toDomain(AssignmentWeightsConfigEntity e) {
        return AssignmentWeightsConfig.reconstitute(e.getId(), e.getVersion(),
            e.getCapacityWeight(), e.getSpecializationWeight(),
            e.getRegionWeight(), e.getCaseloadMixWeight(),
            e.isActive(), e.getEffectiveFrom(), e.getCreatedById(), e.getCreatedAt());
    }
}
