package com.act.casemanagement.application.port;

import com.act.casemanagement.domain.aggregate.CaseTypeConfig;
import com.act.casemanagement.domain.valueobject.CaseCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaseTypeConfigRepositoryPort {
    Optional<CaseTypeConfig> findById(UUID id);
    Optional<CaseTypeConfig> findByCode(CaseCategory code);
    List<CaseTypeConfig> findAllActive();
    CaseTypeConfig save(CaseTypeConfig config);
}
