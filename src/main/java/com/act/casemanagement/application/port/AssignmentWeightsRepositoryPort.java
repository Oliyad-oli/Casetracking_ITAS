package com.act.casemanagement.application.port;

import com.act.casemanagement.domain.aggregate.AssignmentWeightsConfig;

import java.util.Optional;

public interface AssignmentWeightsRepositoryPort {
    Optional<AssignmentWeightsConfig> findActive();
    AssignmentWeightsConfig save(AssignmentWeightsConfig config);
}
