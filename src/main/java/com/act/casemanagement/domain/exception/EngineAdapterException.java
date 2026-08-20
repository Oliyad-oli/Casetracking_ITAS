package com.act.casemanagement.domain.exception;

/** Thrown by engine adapters when a downstream call fails after all retries. Maps to HTTP 503. */
public class EngineAdapterException extends RuntimeException {

    private final String adapterName;

    public EngineAdapterException(String adapterName, String operation, Throwable cause) {
        super("[" + adapterName + "] " + operation + " failed: " + cause.getMessage(), cause);
        this.adapterName = adapterName;
    }

    public String getAdapterName() { return adapterName; }
}
