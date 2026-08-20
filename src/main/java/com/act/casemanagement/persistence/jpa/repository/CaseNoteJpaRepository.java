package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.CaseNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CaseNoteJpaRepository extends JpaRepository<CaseNoteEntity, UUID> {
    List<CaseNoteEntity> findByCaseId(UUID caseId);
}
