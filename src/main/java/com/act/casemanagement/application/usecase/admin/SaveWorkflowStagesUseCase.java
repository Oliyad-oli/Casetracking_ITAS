package com.act.casemanagement.application.usecase.admin;

import com.act.casemanagement.application.port.WorkflowStageRepositoryPort;
import com.act.casemanagement.domain.aggregate.WorkflowStageConfig;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import com.act.casemanagement.domain.valueobject.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveWorkflowStagesUseCase {

    private final WorkflowStageRepositoryPort stageRepository;

    @Transactional
    public void execute(List<StageCommand> stages) {
        List<WorkflowStageConfig> configs = stages.stream()
                .map(s -> WorkflowStageConfig.reconstitute(
                        s.id() != null ? s.id() : UUID.randomUUID(),
                        null, s.name(), s.category(), s.description(),
                        s.status(), s.allowedRoles(), s.allowedActions(),
                        s.slaHours(), s.nextStatus(), true,
                        Instant.now(), Instant.now()))
                .toList();
        stageRepository.saveAll(configs);
    }

    public record StageCommand(
            UUID id,
            String name,
            CaseCategory category,
            String description,
            CaseStatus status,
            List<UserRole> allowedRoles,
            List<String> allowedActions,
            int slaHours,
            CaseStatus nextStatus
    ) {}
}
