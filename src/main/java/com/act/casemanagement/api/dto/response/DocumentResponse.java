package com.act.casemanagement.api.dto.response;

import com.act.casemanagement.domain.model.CaseDocument;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID caseId,
        String name,
        String documentType,
        String fileType,
        BigDecimal sizeInMb,
        UUID uploadedById,
        String uploadedByName,
        Instant uploadedAt,
        String classification,
        String docVersion,
        String dmsReference
) {
    public static DocumentResponse from(CaseDocument d) {
        return new DocumentResponse(
                d.getId(), d.getCaseId(), d.getName(),
                d.getDocumentType().name(), d.getFileType(), d.getSizeInMb(),
                d.getUploadedById(), d.getUploadedByName(), d.getUploadedAt(),
                d.getClassification().name(), d.getDocVersion(), d.getDmsReference());
    }
}
