package com.act.casemanagement.api.controller;

import com.act.casemanagement.api.dto.request.AssociateCasesRequest;
import com.act.casemanagement.application.usecase.association.AssociateCasesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** CTR1000 — Case associations. */
@RestController
@RequestMapping("/cases/{sourceCaseId}/associations")
@Tag(name = "CTR1000", description = "Case Associations (CTR1000)")
@RequiredArgsConstructor
public class AssociationController {

    private final AssociateCasesUseCase associateUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Associate this case with another case")
    public void associate(@PathVariable UUID sourceCaseId,
                          @Valid @RequestBody AssociateCasesRequest req) {
        associateUseCase.execute(req.toCommand(sourceCaseId));
    }
}
