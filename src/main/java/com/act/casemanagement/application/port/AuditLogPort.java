package com.act.casemanagement.application.port;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Append-only audit log port. Exposes ONLY save() — no update or delete.
 * Enforced at the DB level (no UPDATE/DELETE grants) and at this interface level.
 */
public interface AuditLogPort {

    void save(AuditEntry entry);

    List<AuditEntry> findByCaseId(UUID caseId);

    record AuditEntry(
        UUID id,
        Instant occurredAt,
        UUID actorId,
        String actorName,
        String actorRole,
        String action,
        UUID caseId,
        String caseNumber,
        String details,
        String previousValue,
        String newValue,
        String ipAddress,
        String correlationId
    ) {}
}
