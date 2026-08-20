package com.act.casemanagement.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "case_documents")
@Getter @Setter
public class CaseDocumentEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(name = "document_type", nullable = false, length = 60)
    private String documentType;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @Column(name = "size_in_mb", precision = 10, scale = 3)
    private Double sizeInMb;

    @Column(name = "uploaded_by_id", nullable = false)
    private UUID uploadedById;

    @Column(name = "uploaded_by_name", length = 200)
    private String uploadedByName;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(nullable = false, length = 30)
    private String classification;

    @Column(name = "doc_version", length = 20)
    private String docVersion;

    @Column(name = "dms_reference", length = 200)
    private String dmsReference;
}
