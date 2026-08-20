package com.act.casemanagement.persistence.adapter;

import com.act.casemanagement.application.port.IdempotencyStorePort;
import com.act.casemanagement.persistence.jpa.entity.IdempotencyStoreEntity;
import com.act.casemanagement.persistence.jpa.repository.IdempotencyStoreJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyStorePersistenceAdapter implements IdempotencyStorePort {

    private final IdempotencyStoreJpaRepository repo;

    @Override
    public Optional<CachedResponse> findByKey(String idempotencyKey) {
        return repo.findById(idempotencyKey)
            .filter(e -> e.getExpiresAt().isAfter(Instant.now()))
            .map(e -> new CachedResponse(e.getResponseStatus(), e.getResponseBody()));
    }

    @Override
    public void store(String idempotencyKey, int status, String responseBody, long ttlSeconds) {
        IdempotencyStoreEntity e = new IdempotencyStoreEntity();
        e.setIdempotencyKey(idempotencyKey);
        e.setResponseStatus(status);
        e.setResponseBody(responseBody);
        e.setCreatedAt(Instant.now());
        e.setExpiresAt(Instant.now().plusSeconds(ttlSeconds));
        repo.save(e);
    }
}
