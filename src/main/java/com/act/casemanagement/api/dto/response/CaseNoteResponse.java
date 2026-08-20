package com.act.casemanagement.api.dto.response;

import com.act.casemanagement.domain.model.CaseNote;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CaseNoteResponse(
        UUID id,
        UUID caseId,
        String content,
        String originalContent,
        LocalDate eventDate,
        Instant recordingDate,
        UUID authorId,
        String authorName,
        String authorRole,
        boolean obsolete,
        String obsoleteReason,
        boolean edited,
        int editCount,
        String attachmentName
) {
    public static CaseNoteResponse from(CaseNote n) {
        return new CaseNoteResponse(
                n.getId(), n.getCaseId(), n.getContent(), n.getOriginalContent(),
                n.getEventDate(), n.getRecordingDate(),
                n.getAuthorId(), n.getAuthorName(), n.getAuthorRole().name(),
                n.isObsolete(), n.getObsoleteReason(), n.isEdited(),
                n.getEditHistory().size(), n.getAttachmentName());
    }
}
