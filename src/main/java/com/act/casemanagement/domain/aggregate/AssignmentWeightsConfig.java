package com.act.casemanagement.domain.aggregate;

import com.act.casemanagement.domain.exception.DomainException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Admin-configurable scoring weights for AssignmentRecommendationService.
 * Defaults from the frontend (0.35 / 0.35 / 0.15 / 0.15) are seeded via Flyway.
 */
@Getter
public class AssignmentWeightsConfig extends AggregateRoot {

    private final UUID id;
    private Long version;
    private final BigDecimal capacityWeight;
    private final BigDecimal specializationWeight;
    private final BigDecimal regionWeight;
    private final BigDecimal caseloadMixWeight;
    private boolean active;
    private final Instant effectiveFrom;
    private final UUID createdById;
    private final Instant createdAt;

    public static AssignmentWeightsConfig create(UUID id,
            BigDecimal capacityWeight, BigDecimal specializationWeight,
            BigDecimal regionWeight, BigDecimal caseloadMixWeight,
            Instant effectiveFrom, UUID createdById) {
        validateWeights(capacityWeight, specializationWeight, regionWeight, caseloadMixWeight);
        return new AssignmentWeightsConfig(id, null, capacityWeight, specializationWeight,
                regionWeight, caseloadMixWeight, true, effectiveFrom, createdById, Instant.now());
    }

    public static AssignmentWeightsConfig reconstitute(UUID id, Long version,
            BigDecimal capacityWeight, BigDecimal specializationWeight,
            BigDecimal regionWeight, BigDecimal caseloadMixWeight,
            boolean active, Instant effectiveFrom, UUID createdById, Instant createdAt) {
        return new AssignmentWeightsConfig(id, version, capacityWeight, specializationWeight,
                regionWeight, caseloadMixWeight, active, effectiveFrom, createdById, createdAt);
    }

    private static void validateWeights(BigDecimal c, BigDecimal s, BigDecimal r, BigDecimal m) {
        BigDecimal sum = c.add(s).add(r).add(m);
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new DomainException(
                "Assignment weights must sum to 1.0, got: " + sum);
        }
    }

    private AssignmentWeightsConfig(UUID id, Long version,
            BigDecimal capacityWeight, BigDecimal specializationWeight,
            BigDecimal regionWeight, BigDecimal caseloadMixWeight,
            boolean active, Instant effectiveFrom, UUID createdById, Instant createdAt) {
        this.id = id; this.version = version;
        this.capacityWeight = capacityWeight; this.specializationWeight = specializationWeight;
        this.regionWeight = regionWeight; this.caseloadMixWeight = caseloadMixWeight;
        this.active = active; this.effectiveFrom = effectiveFrom;
        this.createdById = createdById; this.createdAt = createdAt;
    }

    @Override public UUID getId() { return id; }
    public void setVersion(Long v) { this.version = v; }
}
