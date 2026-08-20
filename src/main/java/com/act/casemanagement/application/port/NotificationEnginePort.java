package com.act.casemanagement.application.port;

import java.util.Map;
import java.util.UUID;

/**
 * Outbound port for taxpayer/officer notifications (CTR0600 repeat-alert mechanism).
 * Implementations go through the outbox — never called inline from a use case.
 */
public interface NotificationEnginePort {

    /**
     * @param recipientId  UUID of the recipient user (officer or supervisor)
     * @param templateCode notification template code
     * @param variables    template variable substitutions
     * @param correlationId trace correlation id
     */
    void send(UUID recipientId, String templateCode,
              Map<String, Object> variables, UUID correlationId);
}
