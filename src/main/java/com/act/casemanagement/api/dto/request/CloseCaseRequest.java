package com.act.casemanagement.api.dto.request;

import com.act.casemanagement.application.usecase.cases.CloseCaseUseCase;
import com.act.casemanagement.domain.valueobject.CaseClosureDetails;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CloseCaseRequest(
        @NotNull CaseClosureDetails.ClosureReason closureReason,
        @NotNull CaseClosureDetails.ClosureOutcome outcome,
                 String resolutionSummary,
                 BigDecimal amountAssessed,
                 BigDecimal amountCollected,
                 BigDecimal amountWrittenOff,
                 BigDecimal refundApproved,
                 String closedByName,
                 String finalDocumentId
) {
    public CloseCaseUseCase.Command toCommand(UUID caseId) {
        return new CloseCaseUseCase.Command(caseId, closureReason, outcome, resolutionSummary,
                amountAssessed, amountCollected, amountWrittenOff, refundApproved,
                closedByName, finalDocumentId);
    }
}
