package com.act.casemanagement.api.dto.request;

import com.act.casemanagement.application.usecase.association.AssociateCasesUseCase;
import com.act.casemanagement.domain.valueobject.AssociationType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssociateCasesRequest(
        @NotNull UUID targetCaseId,
        @NotNull AssociationType relationshipType,
                 String notes,
                 String actorName
) {
    public AssociateCasesUseCase.Command toCommand(UUID sourceCaseId) {
        return new AssociateCasesUseCase.Command(
                sourceCaseId, targetCaseId, relationshipType,
                notes != null ? notes : "", actorName);
    }
}
