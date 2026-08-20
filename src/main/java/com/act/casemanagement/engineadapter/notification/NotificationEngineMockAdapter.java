package com.act.casemanagement.engineadapter.notification;

import com.act.casemanagement.application.port.NotificationEnginePort;
import com.act.casemanagement.engineadapter.shared.BaseEngineAdapter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * [MOCK] Notification engine adapter.
 * Replace by wiring the real notification-engine client library.
 * Every method has @CircuitBreaker + @Retry with a fallback that throws
 * EngineAdapterException — same discipline as bs-filing-core-server.
 */
@Slf4j
@Component
public class NotificationEngineMockAdapter extends BaseEngineAdapter
        implements NotificationEnginePort {

    public NotificationEngineMockAdapter() {
        super("notification-engine");
    }

    @Override
    @CircuitBreaker(name = "notification-engine", fallbackMethod = "sendFallback")
    @Retry(name = "notification-engine")
    public void send(UUID recipientId, String templateCode,
                     Map<String, Object> variables, UUID correlationId) {
        log.info("[MOCK] notification send recipientId={} template={} correlationId={}",
                recipientId, templateCode, correlationId);
        // Real implementation: call notification-engine HTTP API here
    }

    @SuppressWarnings("unused")
    private void sendFallback(UUID recipientId, String templateCode,
                              Map<String, Object> variables, UUID correlationId,
                              Exception ex) {
        throw wrapException("send", ex);
    }
}
