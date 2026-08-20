package com.act.casemanagement.api.controller;

import com.act.casemanagement.api.dto.request.AddCaseNoteRequest;
import com.act.casemanagement.api.dto.request.UpdateCaseNoteRequest;
import com.act.casemanagement.api.dto.response.CaseNoteResponse;
import com.act.casemanagement.application.usecase.notes.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * CTR0500 — Case note management.
 * Update and obsolete restricted to note author or assigning officer (Section 5.2).
 */
@RestController
@RequestMapping("/cases/{caseId}/notes")
@Tag(name = "CTR0500", description = "Case Note Management (CTR0500)")
@RequiredArgsConstructor
public class CaseNoteController {

    private final AddCaseNoteUseCase      addNoteUseCase;
    private final UpdateCaseNoteUseCase   updateNoteUseCase;
    private final MarkNoteObsoleteUseCase markObsoleteUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a case note")
    public CaseNoteResponse add(@PathVariable UUID caseId,
            @Valid @RequestBody AddCaseNoteRequest req) {
        return CaseNoteResponse.from(addNoteUseCase.execute(req.toCommand(caseId)));
    }

    @PutMapping("/{noteId}")
    @Operation(summary = "Edit a case note — author or assigning officer only (CTR0500 Section 5.2)")
    public CaseNoteResponse update(@PathVariable UUID caseId,
            @PathVariable UUID noteId,
            @Valid @RequestBody UpdateCaseNoteRequest req) {
        return CaseNoteResponse.from(updateNoteUseCase.execute(req.toCommand(caseId, noteId)));
    }

    @PostMapping("/{noteId}/obsolete")
    @Operation(summary = "Mark a note obsolete — author or assigning officer only")
    public CaseNoteResponse markObsolete(@PathVariable UUID caseId,
            @PathVariable UUID noteId,
            @RequestParam String reason) {
        return CaseNoteResponse.from(markObsoleteUseCase.execute(
                new MarkNoteObsoleteUseCase.Command(caseId, noteId, reason)));
    }
}
