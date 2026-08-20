package com.act.casemanagement.api.dto.request;

import com.act.casemanagement.application.usecase.notes.AddCaseNoteUseCase;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record AddCaseNoteRequest(
        @NotBlank String content,
                  LocalDate eventDate,
                  String authorName,
                  String attachmentName
) {
    public AddCaseNoteUseCase.Command toCommand(UUID caseId) {
        return new AddCaseNoteUseCase.Command(caseId, content, eventDate, authorName, attachmentName);
    }
}
