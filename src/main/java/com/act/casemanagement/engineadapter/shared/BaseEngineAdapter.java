package com.act.casemanagement.engineadapter.shared;

import com.act.casemanagement.domain.exception.EngineAdapterException;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for all engine adapters.
 * Provides the wrapException helper that fallback methods must call —
 * matching the exact pattern from bs-filing-core-server BaseEngineAdapter.
 * Every subclass method must carry both @CircuitBreaker and @Retry.
 */
@Slf4j
public abstract class BaseEngineAdapter {

    private final String adapterName;

    protected BaseEngineAdapter(String adapterName) {
        this.adapterName = adapterName;
    }

    protected EngineAdapterException wrapException(String operation, Exception cause) {
        log.error("[{}] {} failed: {}", adapterName, operation, cause.getMessage(), cause);
        return new EngineAdapterException(adapterName, operation, cause);
    }
}
