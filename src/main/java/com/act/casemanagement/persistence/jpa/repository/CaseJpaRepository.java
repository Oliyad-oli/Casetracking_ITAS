package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.CaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaseJpaRepository extends JpaRepository<CaseEntity, UUID> {

    Optional<CaseEntity> findByCaseNumber(String caseNumber);

    List<CaseEntity> findByAssignedOfficerId(UUID officerId);

    List<CaseEntity> findByAssignedUnitId(String unitId);

    List<CaseEntity> findByStatus(String status);

    List<CaseEntity> findByTin(String tin);

    @Query("SELECT c FROM CaseEntity c WHERE c.status != 'CLOSED'")
    List<CaseEntity> findAllActive();

    @Query("SELECT COUNT(c) FROM CaseEntity c WHERE c.assignedOfficerId = :officerId AND c.status != 'CLOSED'")
    long countActiveByOfficerId(@Param("officerId") UUID officerId);

    @Query("SELECT COUNT(c) FROM CaseEntity c WHERE c.assignedOfficerId = :officerId " +
           "AND c.status != 'CLOSED' AND (c.riskLevel IN ('CRITICAL','HIGH') OR c.priority IN ('CRITICAL','HIGH'))")
    long countHighRiskActiveByOfficerId(@Param("officerId") UUID officerId);
}
