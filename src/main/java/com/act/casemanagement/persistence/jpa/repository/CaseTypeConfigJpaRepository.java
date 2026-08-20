package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.CaseTypeConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaseTypeConfigJpaRepository extends JpaRepository<CaseTypeConfigEntity, UUID> {
    Optional<CaseTypeConfigEntity> findByCode(String code);
    List<CaseTypeConfigEntity> findByActiveTrue();
}
