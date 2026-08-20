package com.act.casemanagement.api.dto.request;

import com.act.casemanagement.application.usecase.assignment.AssignCaseUseCase;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** No actorId — comes from header. assigningActorName is display-only. */
public record AssignCaseRequest(
        @NotNull UUID officerId,
                 String assigningActorName,
                 String reason
) {
    public AssignCaseUseCase.Command toCommand(UUID caseId) {
        return new AssignCaseUseCase.Command(caseId, officerId, assigningActorName, reason);
    }
}
