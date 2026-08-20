package com.act.casemanagement.persistence.adapter;

import com.act.casemanagement.application.port.AuditLogPort;
import com.act.casemanagement.persistence.jpa.entity.AuditLogEntity;
import com.act.casemanagement.persistence.jpa.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Append-only — exposes save() and findByCaseId() ONLY.
 * No update or delete at the application layer (Section 3.8 — audit log invariant).
 */
@Component
@RequiredArgsConstructor
public class AuditLogPersistenceAdapter implements AuditLogPort {

    private final AuditLogJpaRepository repo;

    @Override
    public void save(AuditEntry entry) {
        AuditLogEntity e = new AuditLogEntity();
        e.setId(entry.id() != null ? entry.id() : UUID.randomUUID());
        e.setOccurredAt(entry.occurredAt());
        e.setActorId(entry.actorId());
        e.setActorName(entry.actorName());
        e.setActorRole(entry.actorRole());
        e.setAction(entry.action());
        e.setCaseId(entry.caseId());
        e.setCaseNumber(entry.caseNumber());
        e.setDetails(entry.details());
        e.setPreviousValue(entry.previousValue());
        e.setNewValue(entry.newValue());
        e.setIpAddress(entry.ipAddress());
        e.setCorrelationId(entry.correlationId());
        repo.save(e);
    }

    @Override
    public List<AuditEntry> findByCaseId(UUID caseId) {
        return repo.findByCaseIdOrderByOccurredAtDesc(caseId).stream()
            .map(e -> new AuditEntry(e.getId(), e.getOccurredAt(), e.getActorId(),
                e.getActorName(), e.getActorRole(), e.getAction(), e.getCaseId(),
                e.getCaseNumber(), e.getDetails(), e.getPreviousValue(),
                e.getNewValue(), e.getIpAddress(), e.getCorrelationId()))
            .toList();
    }
}
