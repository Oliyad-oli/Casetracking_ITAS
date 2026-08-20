package com.act.casemanagement.engineadapter.userprojection;

import com.act.casemanagement.application.port.UserRepositoryPort;
import com.act.casemanagement.domain.valueobject.UserRole;
import com.act.casemanagement.engineadapter.shared.BaseEngineAdapter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * [STUB] User projection adapter.
 *
 * In the full platform, this would query a local read-model table kept fresh
 * by a registration-service event feed (same as filing-service Rule 10 —
 * "registration data via projection only, never call registration-service directly").
 *
 * For this pass: holds an in-memory registry seeded at startup.
 * Replace the inner store with a Spring Data JPA projection when the
 * platform-wide user-sync feed is available.
 */
@Slf4j
@Component
public class UserProjectionAdapter extends BaseEngineAdapter implements UserRepositoryPort {

    public UserProjectionAdapter() {
        super("user-projection");
    }

    // In-memory store — replace with JPA projection table when sync feed exists
    private final ConcurrentHashMap<UUID, UserRecord> store = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<UserRecord>    all   = new CopyOnWriteArrayList<>();

    /** Called by UserProjectionSeedConfig at startup to populate seed data. */
    public void seed(UserRecord record) {
        store.put(record.id(), record);
        all.removeIf(u -> u.id().equals(record.id()));
        all.add(record);
    }

    @Override
    @CircuitBreaker(name = "user-projection", fallbackMethod = "findByIdFallback")
    @Retry(name = "user-projection")
    public Optional<UserRecord> findById(UUID userId) {
        return Optional.ofNullable(store.get(userId));
    }

    @Override
    public List<UserRecord> findByRole(UserRole role) {
        return all.stream().filter(u -> u.role() == role).toList();
    }

    @Override
    public List<UserRecord> findByUnitId(String unitId) {
        return all.stream().filter(u -> unitId.equals(u.unitId())).toList();
    }

    @Override
    public List<UserRecord> findEligibleOfficers() {
        return all.stream()
                .filter(u -> u.role() == UserRole.TAX_OFFICER || u.role() == UserRole.SUPERVISOR)
                .filter(u -> "ACTIVE".equals(u.status()))
                .toList();
    }

    @SuppressWarnings("unused")
    private Optional<UserRecord> findByIdFallback(UUID userId, Exception ex) {
        log.warn("[user-projection] findById fallback userId={}", userId);
        return Optional.empty();
    }
}
