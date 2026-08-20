package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.CaseDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CaseDocumentJpaRepository extends JpaRepository<CaseDocumentEntity, UUID> {
    List<CaseDocumentEntity> findByCaseId(UUID caseId);
}
