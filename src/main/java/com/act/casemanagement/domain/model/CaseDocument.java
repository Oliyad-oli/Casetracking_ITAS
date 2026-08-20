package com.act.casemanagement.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Document reference entity — child of the Case aggregate. */
@Getter
public class CaseDocument {

    public enum DocumentType {
        EVIDENCE, CORRESPONDENCE, ASSESSMENT, LEGAL_BRIEF,
        TAXPAYER_SUBMISSION, CLOSURE_NOTICE, SUPPORTING
    }

    public enum SecurityClassification {
        PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED, HIGHLY_RESTRICTED
    }

    private final UUID id;
    private final UUID caseId;
    private final String name;
    private final DocumentType documentType;
    private final String fileType;
    private final BigDecimal sizeInMb;
    private final UUID uploadedById;
    private final String uploadedByName;
    private final Instant uploadedAt;
    private final SecurityClassification classification;
    private final String docVersion;
    private final String dmsReference;
    private Long version;

    public static CaseDocument create(UUID id, UUID caseId, String name,
            DocumentType documentType, String fileType, BigDecimal sizeInMb,
            UUID uploadedById, String uploadedByName, Instant uploadedAt,
            SecurityClassification classification, String docVersion,
            String dmsReference) {
        return new CaseDocument(id, caseId, name, documentType, fileType, sizeInMb,
                uploadedById, uploadedByName, uploadedAt, classification,
                docVersion, dmsReference, null);
    }

    public static CaseDocument reconstitute(UUID id, UUID caseId, String name,
            DocumentType documentType, String fileType, BigDecimal sizeInMb,
            UUID uploadedById, String uploadedByName, Instant uploadedAt,
            SecurityClassification classification, String docVersion,
            String dmsReference, Long version) {
        return new CaseDocument(id, caseId, name, documentType, fileType, sizeInMb,
                uploadedById, uploadedByName, uploadedAt, classification,
                docVersion, dmsReference, version);
    }

    private CaseDocument(UUID id, UUID caseId, String name, DocumentType documentType,
            String fileType, BigDecimal sizeInMb, UUID uploadedById,
            String uploadedByName, Instant uploadedAt,
            SecurityClassification classification, String docVersion,
            String dmsReference, Long version) {
        this.id             = id;
        this.caseId         = caseId;
        this.name           = name;
        this.documentType   = documentType;
        this.fileType       = fileType;
        this.sizeInMb       = sizeInMb;
        this.uploadedById   = uploadedById;
        this.uploadedByName = uploadedByName;
        this.uploadedAt     = uploadedAt;
        this.classification = classification;
        this.docVersion     = docVersion;
        this.dmsReference   = dmsReference;
        this.version        = version;
    }

    public void setVersion(Long version) { this.version = version; }
}
