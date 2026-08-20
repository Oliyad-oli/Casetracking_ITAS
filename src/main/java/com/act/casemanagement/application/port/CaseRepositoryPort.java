package com.act.casemanagement.application.port;

import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaseRepositoryPort {
    Case save(Case aCase);
    Optional<Case> findById(UUID id);
    Optional<Case> findByCaseNumber(String caseNumber);
    List<Case> findByAssignedOfficerId(UUID officerId);
    List<Case> findByAssignedUnitId(String unitId);
    List<Case> findByStatus(CaseStatus status);
    List<Case> findByTin(String tin);
    List<Case> findAll();
    List<Case> findAllActive();  // all non-CLOSED
    long countActiveByOfficerId(UUID officerId);
    long countHighRiskActiveByOfficerId(UUID officerId); // CRITICAL or HIGH risk
}
