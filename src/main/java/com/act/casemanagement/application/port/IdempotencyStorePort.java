package com.act.casemanagement.application.port;

import java.util.Optional;

/** POST replay-protection store — mirrors the filing-service pattern. */
public interface IdempotencyStorePort {

    Optional<CachedResponse> findByKey(String idempotencyKey);

    void store(String idempotencyKey, int status, String responseBody, long ttlSeconds);

    record CachedResponse(int status, String responseBody) {}
}
