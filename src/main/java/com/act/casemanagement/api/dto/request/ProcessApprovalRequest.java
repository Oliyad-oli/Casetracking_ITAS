package com.act.casemanagement.api.dto.request;

import com.act.casemanagement.application.usecase.approval.ProcessApprovalUseCase;
import com.act.casemanagement.domain.valueobject.ApprovalAction;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProcessApprovalRequest(
        @NotNull ApprovalAction action,
                 String approverName,
                 String comments
) {
    public ProcessApprovalUseCase.Command toCommand(UUID caseId) {
        return new ProcessApprovalUseCase.Command(caseId, action, approverName, comments);
    }
}
