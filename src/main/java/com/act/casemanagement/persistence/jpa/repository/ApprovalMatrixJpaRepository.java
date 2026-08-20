package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.ApprovalMatrixConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalMatrixJpaRepository extends JpaRepository<ApprovalMatrixConfigEntity, UUID> {
    List<ApprovalMatrixConfigEntity> findByActiveTrue();
}
