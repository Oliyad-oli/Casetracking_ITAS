package com.act.casemanagement.api.controller;

import com.act.casemanagement.api.dto.request.AssignCaseRequest;
import com.act.casemanagement.api.dto.response.CaseResponse;
import com.act.casemanagement.api.dto.response.RecommendationResponse;
import com.act.casemanagement.application.usecase.assignment.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * CTR0200/CTR0300 — Case assignment, reassignment, deassignment, recommendations.
 */
@RestController
@RequestMapping("/cases")
@Tag(name = "CTR0200", description = "Case Assignment (CTR0200/CTR0300)")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignCaseUseCase                       assignUseCase;
    private final ReassignCaseUseCase                     reassignUseCase;
    private final DeassignCaseUseCase                     deassignUseCase;
    private final GetAssignmentRecommendationsUseCase     recommendationsUseCase;

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign case to an officer (CTR0200/CTR0300 — self-assignment requires justification)")
    public CaseResponse assign(@PathVariable UUID id,
            @Valid @RequestBody AssignCaseRequest req) {
        return CaseResponse.from(assignUseCase.execute(req.toCommand(id)));
    }

    @PostMapping("/{id}/reassign")
    @Operation(summary = "Reassign case to a different officer")
    public CaseResponse reassign(@PathVariable UUID id,
            @Valid @RequestBody AssignCaseRequest req) {
        return CaseResponse.from(reassignUseCase.execute(
                new ReassignCaseUseCase.Command(id, req.officerId(),
                        req.assigningActorName(), req.reason())));
    }

    @PostMapping("/{id}/deassign")
    @Operation(summary = "Remove officer assignment from case")
    public CaseResponse deassign(@PathVariable UUID id,
            @RequestParam(defaultValue = "Manual deassignment") String reason) {
        return CaseResponse.from(deassignUseCase.execute(
                new DeassignCaseUseCase.Command(id, reason)));
    }

    @GetMapping("/{id}/assignment-recommendations")
    @Operation(summary = "Weighted officer recommendations for this case")
    public List<RecommendationResponse> recommendations(@PathVariable UUID id) {
        return recommendationsUseCase.execute(id).stream()
                .map(RecommendationResponse::from).toList();
    }
}
