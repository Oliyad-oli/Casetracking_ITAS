package com.act.casemanagement.persistence.adapter;

import com.act.casemanagement.application.port.OutboxPort;
import com.act.casemanagement.domain.aggregate.OutboxEntry;
import com.act.casemanagement.persistence.jpa.entity.OutboxEntryEntity;
import com.act.casemanagement.persistence.jpa.repository.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxPersistenceAdapter implements OutboxPort {

    private final OutboxJpaRepository repo;

    @Override
    public OutboxEntry save(OutboxEntry entry) {
        repo.save(toEntity(entry));
        return entry;
    }

    @Override
    public List<OutboxEntry> findDispatchable(int limit) {
        return repo.findDispatchable(Instant.now(), limit).stream()
            .map(this::toDomain).toList();
    }

    @Override
    public OutboxEntry findById(UUID id) {
        return repo.findById(id).map(this::toDomain)
            .orElseThrow(() -> new IllegalStateException("OutboxEntry not found: " + id));
    }

    @Override
    public void update(OutboxEntry entry) {
        repo.save(toEntity(entry));
    }

    private OutboxEntryEntity toEntity(OutboxEntry e) {
        OutboxEntryEntity entity = new OutboxEntryEntity();
        entity.setId(e.getId());
        entity.setEventType(e.getEventType());
        entity.setPayload(e.getPayload());
        entity.setAggregateId(e.getAggregateId());
        entity.setAggregateType(e.getAggregateType());
        entity.setStatus(e.getStatus().name());
        entity.setAttempts(e.getAttempts());
        entity.setMaxAttempts(e.getMaxAttempts());
        entity.setLastError(e.getLastError());
        entity.setCreatedAt(e.getCreatedAt());
        entity.setNextAttemptAt(e.getNextAttemptAt());
        entity.setSentAt(e.getSentAt());
        return entity;
    }

    private OutboxEntry toDomain(OutboxEntryEntity e) {
        return OutboxEntry.reconstitute(e.getId(), e.getEventType(), e.getPayload(),
            e.getAggregateId(), e.getAggregateType(),
            OutboxEntry.Status.valueOf(e.getStatus()),
            e.getAttempts(), e.getMaxAttempts(), e.getLastError(),
            e.getCreatedAt(), e.getNextAttemptAt(), e.getSentAt());
    }
}
