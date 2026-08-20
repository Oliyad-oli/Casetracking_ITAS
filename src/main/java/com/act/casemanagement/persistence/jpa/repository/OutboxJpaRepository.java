package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.OutboxEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEntryEntity, UUID> {

    @Query("SELECT o FROM OutboxEntryEntity o WHERE o.status IN ('PENDING','RETRY') " +
           "AND o.nextAttemptAt <= :now ORDER BY o.createdAt ASC LIMIT :limit")
    List<OutboxEntryEntity> findDispatchable(@Param("now") Instant now, @Param("limit") int limit);
}
