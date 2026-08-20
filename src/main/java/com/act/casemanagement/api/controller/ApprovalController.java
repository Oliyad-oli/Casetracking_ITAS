package com.act.casemanagement.api.controller;

import com.act.casemanagement.api.dto.request.ProcessApprovalRequest;
import com.act.casemanagement.api.dto.response.CaseResponse;
import com.act.casemanagement.application.usecase.approval.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** CTR0800 — Approval chain processing. */
@RestController
@RequestMapping("/cases")
@Tag(name = "CTR0800", description = "Approval Chain (CTR0800)")
@RequiredArgsConstructor
public class ApprovalController {

    private final SubmitForApprovalUseCase  submitUseCase;
    private final ProcessApprovalUseCase    processUseCase;

    @PostMapping("/{id}/submit-for-approval")
    @Operation(summary = "Submit case for approval — walks the matrix-defined chain (CTR0800)")
    public CaseResponse submit(@PathVariable UUID id,
            @RequestParam(defaultValue = "") String comments) {
        return CaseResponse.from(submitUseCase.execute(id, comments));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Process an approval decision (APPROVED/REJECTED/REQUESTED_INFO/ESCALATED)")
    public CaseResponse processApproval(@PathVariable UUID id,
            @Valid @RequestBody ProcessApprovalRequest req) {
        return CaseResponse.from(processUseCase.execute(req.toCommand(id)));
    }
}
