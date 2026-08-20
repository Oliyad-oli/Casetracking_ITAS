package com.act.casemanagement.application.usecase.cases;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.*;
import com.act.casemanagement.observability.audit.Auditable;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCaseUseCase {

    private final CaseRepositoryPort  caseRepository;
    private final EventPublisherPort  eventPublisher;

    @Auditable(action = "CREATE_CASE")
    @Transactional
    public Case execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        // Generate a case number: ITAS-REV-{year}-{random 5-digit}
        String caseNumber = "ITAS-REV-" + java.time.Year.now().getValue()
                + "-" + String.format("%05d", (int)(Math.random() * 90000 + 10000));

        Case aCase = Case.create(
            UUID.randomUUID(),
            caseNumber,
            cmd.title(),
            cmd.category(),
            cmd.priority(),
            cmd.riskLevel(),
            new TIN(cmd.tin()),
            cmd.taxpayerName(),
            cmd.entityType(),
            actor.getActorId(),
            cmd.createdByName(),
            cmd.deadline(),
            cmd.targetDays() > 0 ? cmd.targetDays() : 45,
            MonetaryAmount.of(cmd.estimatedAmountEtb() != null ? cmd.estimatedAmountEtb() : BigDecimal.ZERO),
            cmd.reasons(),
            cmd.instructions(),
            cmd.remarks(),
            cmd.typeSpecificData()
        );

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
        return saved;
    }

    public record Command(
        String title,
        CaseCategory category,
        Priority priority,
        RiskLevel riskLevel,
        String tin,
        String taxpayerName,
        String entityType,
        String createdByName,
        Instant deadline,
        int targetDays,
        BigDecimal estimatedAmountEtb,
        String reasons,
        String instructions,
        String remarks,
        Map<String, Object> typeSpecificData
    ) {}
}
