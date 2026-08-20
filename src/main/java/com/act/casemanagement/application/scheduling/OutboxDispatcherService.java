package com.act.casemanagement.application.scheduling;

import com.act.casemanagement.application.port.NotificationEnginePort;
import com.act.casemanagement.application.port.OutboxPort;
import com.act.casemanagement.domain.aggregate.OutboxEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Drains PENDING/RETRY outbox entries with exponential backoff.
 * Each dispatch is its own transaction — one failure does not roll back others.
 * Same pattern as bs-filing-core-server OutboxDispatcher.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxDispatcherService {

    private final OutboxPort              outboxPort;
    private final NotificationEnginePort  notificationEngine;

    @Value("${outbox.dispatcher.batch-size:50}")
    private int batchSize;

    @Value("${outbox.dispatcher.backoff-multiplier:2}")
    private long backoffMultiplier;

    @Scheduled(fixedDelayString = "${outbox.dispatcher.poll-interval-ms:5000}")
    public void dispatch() {
        List<OutboxEntry> entries = outboxPort.findDispatchable(batchSize);
        if (entries.isEmpty()) return;
        log.debug("OutboxDispatcher: dispatching {} entries", entries.size());
        entries.forEach(this::dispatchOne);
    }

    @Transactional
    public void dispatchOne(OutboxEntry entry) {
        try {
            route(entry);
            entry.markSent();
        } catch (Exception e) {
            long backoff = (long) Math.pow(backoffMultiplier, entry.getAttempts()) * 10L;
            entry.recordFailure(e.getMessage(), backoff);
            log.warn("OutboxDispatcher: entry={} attempt={} failed: {}",
                    entry.getId(), entry.getAttempts(), e.getMessage());
        }
        outboxPort.update(entry);
    }

    private void route(OutboxEntry entry) {
        switch (entry.getEventType()) {
            case "CORRESPONDENCE_SEND" -> {
                Map<String, Object> p = entry.getPayload();
                notificationEngine.send(
                        null,   // recipient resolved server-side by template
                        "CORRESPONDENCE",
                        p,
                        entry.getId()
                );
            }
            case "CASE_NOTIFICATION" -> {
                Map<String, Object> p = entry.getPayload();
                String recipientIdStr = (String) p.get("recipientId");
                if (recipientIdStr != null) {
                    notificationEngine.send(
                            UUID.fromString(recipientIdStr),
                            (String) p.getOrDefault("templateCode", "GENERIC"),
                            p,
                            entry.getId()
                    );
                }
            }
            default -> log.warn("OutboxDispatcher: no route for eventType={}",
                    entry.getEventType());
        }
    }
}
