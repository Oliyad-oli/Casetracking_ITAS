package com.act.casemanagement.persistence.jpa.entity;

import com.act.casemanagement.persistence.converter.MapListJsonbConverter;
import com.act.casemanagement.persistence.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "case_type_configs")
@Getter @Setter
public class CaseTypeConfigEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "config_version", length = 20)
    private String configVersion;

    @Column(name = "default_target_days", nullable = false)
    private int defaultTargetDays;

    @Convert(converter = MapListJsonbConverter.class)
    @Column(name = "fields_definition", columnDefinition = "JSONB")
    private List<Map<String, Object>> fieldsDefinition;

    @Convert(converter = StringListConverter.class)
    @Column(name = "allowed_statuses", columnDefinition = "JSONB")
    private List<String> allowedStatuses;

    @Convert(converter = StringListConverter.class)
    @Column(name = "approval_levels", columnDefinition = "JSONB")
    private List<String> approvalLevels;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
