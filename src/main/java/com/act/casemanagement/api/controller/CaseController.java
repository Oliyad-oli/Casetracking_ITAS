package com.act.casemanagement.api.controller;

import com.act.casemanagement.api.dto.request.*;
import com.act.casemanagement.api.dto.response.CaseResponse;
import com.act.casemanagement.api.dto.response.StatsResponse;
import com.act.casemanagement.application.usecase.cases.*;
import com.act.casemanagement.application.usecase.stats.GetActivityStatisticsUseCase;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.CaseStatus;
import com.act.casemanagement.domain.valueobject.Priority;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * CTR0100 — Case lifecycle management.
 * Controllers are pure orchestration — no business logic here.
 * Actor identity is read from RequestActorContext (set by RequestActorFilter from headers).
 */
@RestController
@RequestMapping("/cases")
@Tag(name = "CTR0100", description = "Case Management — create, retrieve, update, close")
@RequiredArgsConstructor
public class CaseController {

    private final CreateCaseUseCase          createCaseUseCase;
    private final GetCaseUseCase             getCaseUseCase;
    private final UpdateCasePriorityUseCase  updatePriorityUseCase;
    private final SearchCasesUseCase         searchCasesUseCase;
    private final CloseCaseUseCase           closeCaseUseCase;
    private final GetActivityStatisticsUseCase statsUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new case (CTR0100)")
    public CaseResponse create(@Valid @RequestBody CreateCaseRequest req) {
        return CaseResponse.from(createCaseUseCase.execute(req.toCommand()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get case by id")
    public CaseResponse getById(@PathVariable UUID id) {
        return CaseResponse.from(getCaseUseCase.execute(id));
    }

    @GetMapping
    @Operation(summary = "Search/list cases — scope applied from actor context (CTR0900)")
    public List<CaseResponse> search(
            @RequestParam(required = false) CaseCategory category,
            @RequestParam(required = false) CaseStatus   status,
            @RequestParam(required = false) String       tin,
            @RequestParam(required = false) String       keyword) {
        return searchCasesUseCase.execute(
                new SearchCasesUseCase.Query(category, status, tin, keyword))
                .stream().map(CaseResponse::from).toList();
    }

    @PatchMapping("/{id}/priority")
    @Operation(summary = "Update case priority")
    public CaseResponse updatePriority(@PathVariable UUID id,
            @RequestParam Priority priority,
            @RequestParam(required = false) String reason) {
        return CaseResponse.from(updatePriorityUseCase.execute(
                new UpdateCasePriorityUseCase.Command(id, priority, reason)));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a case — assigning officer or directorate override only (CTR0700)")
    public CaseResponse close(@PathVariable UUID id,
            @Valid @RequestBody CloseCaseRequest req) {
        return CaseResponse.from(closeCaseUseCase.execute(req.toCommand(id)));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Activity statistics — scoped by actor role (CTR1000/CTR1100)")
    public StatsResponse statistics() {
        return StatsResponse.from(statsUseCase.execute());
    }
}
