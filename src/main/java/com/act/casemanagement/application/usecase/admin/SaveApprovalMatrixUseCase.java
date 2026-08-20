package com.act.casemanagement.application.usecase.admin;

import com.act.casemanagement.application.port.ApprovalMatrixRepositoryPort;
import com.act.casemanagement.domain.aggregate.ApprovalMatrixConfig;
import com.act.casemanagement.domain.valueobject.ApprovalLevel;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.RiskLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveApprovalMatrixUseCase {

    private final ApprovalMatrixRepositoryPort matrixRepository;

    @Transactional
    public void execute(List<RuleCommand> rules) {
        List<ApprovalMatrixConfig> configs = rules.stream()
                .map(r -> ApprovalMatrixConfig.create(
                        r.id() != null ? r.id() : UUID.randomUUID(),
                        r.caseType(), r.minValueEtb(), r.maxValueEtb(),
                        r.riskLevel(), r.requiredApprovals()))
                .toList();
        matrixRepository.saveAll(configs);
    }

    public record RuleCommand(
            UUID id,
            CaseCategory caseType,
            BigDecimal minValueEtb,
            BigDecimal maxValueEtb,
            RiskLevel riskLevel,
            List<ApprovalLevel> requiredApprovals
    ) {}
}
