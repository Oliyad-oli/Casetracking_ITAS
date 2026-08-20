package com.act.casemanagement.persistence.jpa.entity;

import com.act.casemanagement.persistence.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "approval_matrix_configs")
@Getter @Setter
public class ApprovalMatrixConfigEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "case_type", nullable = false, length = 60)
    private String caseType;          // "ALL" or CaseCategory name

    @Column(name = "min_value_etb", nullable = false, precision = 20, scale = 2)
    private BigDecimal minValueEtb;

    @Column(name = "max_value_etb", precision = 20, scale = 2)
    private BigDecimal maxValueEtb;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;         // "ALL" or RiskLevel name

    @Convert(converter = StringListConverter.class)
    @Column(name = "required_approvals", nullable = false, columnDefinition = "JSONB")
    private List<String> requiredApprovals;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
