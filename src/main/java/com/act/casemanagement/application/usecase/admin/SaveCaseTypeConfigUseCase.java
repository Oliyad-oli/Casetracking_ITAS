package com.act.casemanagement.application.usecase.admin;

import com.act.casemanagement.application.port.CaseTypeConfigRepositoryPort;
import com.act.casemanagement.domain.aggregate.CaseTypeConfig;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveCaseTypeConfigUseCase {

    private final CaseTypeConfigRepositoryPort configRepository;

    @Transactional
    public CaseTypeConfig execute(Command cmd) {
        CaseTypeConfig config = configRepository.findById(cmd.id())
                .map(existing -> {
                    // Re-create with new values (immutable-rows pattern — never mutate in place)
                    return CaseTypeConfig.reconstitute(
                            existing.getId(), existing.getVersion(),
                            cmd.code(), cmd.name(), cmd.description(),
                            cmd.active(), cmd.effectiveDate(), cmd.configVersion(),
                            cmd.defaultTargetDays(), cmd.fieldsDefinition(),
                            cmd.allowedStatuses(), cmd.approvalLevels(),
                            existing.getCreatedAt(), java.time.Instant.now());
                })
                .orElseGet(() -> CaseTypeConfig.create(
                        cmd.id() != null ? cmd.id() : UUID.randomUUID(),
                        cmd.code(), cmd.name(), cmd.description(),
                        cmd.effectiveDate(), cmd.configVersion(),
                        cmd.defaultTargetDays(), cmd.fieldsDefinition(),
                        cmd.allowedStatuses(), cmd.approvalLevels()));

        return configRepository.save(config);
    }

    public record Command(
            UUID id,
            CaseCategory code,
            String name,
            String description,
            boolean active,
            LocalDate effectiveDate,
            String configVersion,
            int defaultTargetDays,
            List<Map<String, Object>> fieldsDefinition,
            List<CaseStatus> allowedStatuses,
            List<ApprovalLevel> approvalLevels
    ) {}
}
