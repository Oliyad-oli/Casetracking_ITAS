package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.ApprovalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalRecordJpaRepository extends JpaRepository<ApprovalRecordEntity, UUID> {
    List<ApprovalRecordEntity> findByCaseId(UUID caseId);
}
