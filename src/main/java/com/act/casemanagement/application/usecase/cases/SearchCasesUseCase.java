package com.act.casemanagement.application.usecase.cases;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.service.ReportScopeService;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * CTR0900 — Case enquiry / search.
 * <p>
 * Section 5.2: ReportScopeService is applied using the TRUSTED actor from
 * RequestActorContext — never from a client-supplied filter claiming "for user X".
 */
@Service
@RequiredArgsConstructor
public class SearchCasesUseCase {

    private final CaseRepositoryPort  caseRepository;
    private final ReportScopeService  reportScopeService;

    @Transactional(readOnly = true)
    public List<Case> execute(Query query) {
        RequestActorContext actor = RequestActorContext.current();
        ReportScopeService.ScopeDescriptor scope =
            reportScopeService.deriveScope(actor.getActorId(), actor.getRole(), actor.getUnitId());

        List<Case> all = switch (scope.scopeType()) {
            case PERSONAL      -> caseRepository.findByAssignedOfficerId(actor.getActorId());
            case TEAM          -> actor.getUnitId() != null
                                  ? caseRepository.findByAssignedUnitId(actor.getUnitId())
                                  : caseRepository.findByAssignedOfficerId(actor.getActorId());
            case ORGANIZATION  -> caseRepository.findAll();
        };

        // Apply optional client-side filters (category, status, tin)
        return all.stream()
            .filter(c -> query.category() == null || c.getCategory() == query.category())
            .filter(c -> query.status()   == null || c.getStatus()   == query.status())
            .filter(c -> query.tin()      == null || c.getTin().value().equals(query.tin()))
            .filter(c -> query.keyword()  == null || query.keyword().isBlank()
                || c.getTitle().toLowerCase().contains(query.keyword().toLowerCase())
                || c.getCaseNumber().toLowerCase().contains(query.keyword().toLowerCase()))
            .toList();
    }

    public record Query(
        CaseCategory category,
        CaseStatus   status,
        String       tin,
        String       keyword
    ) {}
}
