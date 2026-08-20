package com.act.casemanagement.persistence.jpa.repository;

import com.act.casemanagement.persistence.jpa.entity.IdempotencyStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface IdempotencyStoreJpaRepository extends JpaRepository<IdempotencyStoreEntity, String> {
    @Modifying
    @Transactional
    @Query("DELETE FROM IdempotencyStoreEntity e WHERE e.expiresAt < :now")
    void deleteExpired(@Param("now") Instant now);
}
