package com.act.casemanagement.application.port;

import com.act.casemanagement.domain.aggregate.OutboxEntry;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxPort {
    OutboxEntry save(OutboxEntry entry);
    List<OutboxEntry> findDispatchable(int limit);  // PENDING or RETRY with nextAttemptAt <= now
    OutboxEntry findById(UUID id);
    void update(OutboxEntry entry);
}
