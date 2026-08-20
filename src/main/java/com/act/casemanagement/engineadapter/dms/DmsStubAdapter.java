package com.act.casemanagement.engineadapter.dms;

import com.act.casemanagement.application.port.DmsPort;
import com.act.casemanagement.engineadapter.shared.BaseEngineAdapter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * [STUB] Document Management System adapter.
 *
 * The platform-wide DMS service integration is not yet confirmed for case management.
 * This stub returns a generated reference so the domain model is fully wired.
 *
 * Integration decision tracked in AGENTS.md section 8 (cross-service note):
 * when the DMS service is confirmed, replace this stub with the real HTTP adapter
 * — same pattern as bs-filing-core-server's DMS adapter for filing certificates.
 *
 * Every method has @CircuitBreaker + @Retry as required by AGENTS.md section 3.6.
 */
@Slf4j
@Component
public class DmsStubAdapter extends BaseEngineAdapter implements DmsPort {

    public DmsStubAdapter() {
        super("dms");
    }

    @Override
    @CircuitBreaker(name = "dms", fallbackMethod = "storeFallback")
    @Retry(name = "dms")
    public String store(UUID caseId, String fileName, String contentType,
                        byte[] content, UUID correlationId) {
        String ref = "DMS-STUB-" + UUID.randomUUID();
        log.info("[STUB] DMS store caseId={} fileName={} ref={} correlationId={}",
                caseId, fileName, ref, correlationId);
        return ref;
    }

    @Override
    @CircuitBreaker(name = "dms", fallbackMethod = "retrieveFallback")
    @Retry(name = "dms")
    public byte[] retrieve(String dmsReference, UUID correlationId) {
        log.info("[STUB] DMS retrieve ref={} correlationId={}", dmsReference, correlationId);
        return new byte[0];
    }

    @SuppressWarnings("unused")
    private String storeFallback(UUID caseId, String fileName, String contentType,
                                 byte[] content, UUID correlationId, Exception ex) {
        throw wrapException("store", ex);
    }

    @SuppressWarnings("unused")
    private byte[] retrieveFallback(String dmsReference, UUID correlationId, Exception ex) {
        throw wrapException("retrieve", ex);
    }
}
