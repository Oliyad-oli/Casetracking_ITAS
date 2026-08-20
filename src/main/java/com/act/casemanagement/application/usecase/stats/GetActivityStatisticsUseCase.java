package com.act.casemanagement.application.usecase.stats;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.service.ReportScopeService;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import com.act.casemanagement.domain.valueobject.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CTR1000/CTR1100 — Activity statistics dashboard.
 * Section 5.2: scope applied from trusted actor context, never from
 * a client-supplied "for user X" parameter.
 */
@Service
@RequiredArgsConstructor
public class GetActivityStatisticsUseCase {

    private final CaseRepositoryPort caseRepository;
    private final ReportScopeService reportScopeService;

    @Transactional(readOnly = true)
    public Stats execute() {
        RequestActorContext actor = RequestActorContext.current();
        ReportScopeService.ScopeDescriptor scope =
                reportScopeService.deriveScope(actor.getActorId(), actor.getRole(), actor.getUnitId());

        List<Case> cases = switch (scope.scopeType()) {
            case PERSONAL     -> caseRepository.findByAssignedOfficerId(actor.getActorId());
            case TEAM         -> actor.getUnitId() != null
                                 ? caseRepository.findByAssignedUnitId(actor.getUnitId())
                                 : caseRepository.findByAssignedOfficerId(actor.getActorId());
            case ORGANIZATION -> caseRepository.findAll();
        };

        long total       = cases.size();
        long open        = cases.stream().filter(c -> c.getStatus() != CaseStatus.CLOSED).count();
        long closed      = cases.stream().filter(c -> c.getStatus() == CaseStatus.CLOSED).count();
        long unassigned  = cases.stream().filter(c -> c.getStatus() == CaseStatus.UNASSIGNED).count();
        long critical    = cases.stream().filter(c -> c.getPriority() == Priority.CRITICAL).count();
        long pendingApproval = cases.stream()
                .filter(c -> c.getStatus().isPendingApproval()).count();

        Map<String, Long> byCategory = cases.stream()
                .collect(Collectors.groupingBy(c -> c.getCategory().name(), Collectors.counting()));
        Map<String, Long> byStatus   = cases.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));

        return new Stats(total, open, closed, unassigned, critical,
                pendingApproval, byCategory, byStatus,
                scope.scopeType().name(), scope.scopeLabel());
    }

    public record Stats(
            long totalCases,
            long openCases,
            long closedCases,
            long unassignedCases,
            long criticalCases,
            long pendingApproval,
            Map<String, Long> byCategory,
            Map<String, Long> byStatus,
            String scopeType,
            String scopeLabel
    ) {}
}
