package com.act.casemanagement.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assignment_weights_configs")
@Getter @Setter
public class AssignmentWeightsConfigEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "capacity_weight", nullable = false, precision = 5, scale = 4)
    private BigDecimal capacityWeight;

    @Column(name = "specialization_weight", nullable = false, precision = 5, scale = 4)
    private BigDecimal specializationWeight;

    @Column(name = "region_weight", nullable = false, precision = 5, scale = 4)
    private BigDecimal regionWeight;

    @Column(name = "caseload_mix_weight", nullable = false, precision = 5, scale = 4)
    private BigDecimal caseloadMixWeight;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "created_by_id")
    private UUID createdById;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
