package com.act.casemanagement.api.dto.request;

import com.act.casemanagement.application.usecase.notes.UpdateCaseNoteUseCase;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateCaseNoteRequest(
        @NotBlank String newContent,
                  String editorName,
        @NotBlank String reason
) {
    public UpdateCaseNoteUseCase.Command toCommand(UUID caseId, UUID noteId) {
        return new UpdateCaseNoteUseCase.Command(caseId, noteId, newContent, editorName, reason);
    }
}
