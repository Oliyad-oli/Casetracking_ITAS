package com.act.casemanagement.api.controller;

import com.act.casemanagement.application.usecase.documents.AddDocumentUseCase;
import com.act.casemanagement.api.dto.response.DocumentResponse;
import com.act.casemanagement.domain.model.CaseDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * CTR0400 — Document management.
 * Accepts multipart/form-data uploads; content stored via DmsPort (stub until
 * the platform DMS service is confirmed — see AGENTS.md section 8).
 */
@RestController
@RequestMapping("/cases/{caseId}/documents")
@Tag(name = "CTR0400", description = "Case Document Management")
@RequiredArgsConstructor
public class DocumentController {

    private final AddDocumentUseCase addDocumentUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a document to a case")
    public DocumentResponse upload(
            @PathVariable UUID caseId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "SUPPORTING") CaseDocument.DocumentType documentType,
            @RequestParam(defaultValue = "INTERNAL") CaseDocument.SecurityClassification classification,
            @RequestParam(required = false) String docVersion,
            @RequestParam(required = false) String uploaderName) throws IOException {

        String fileType = getExtension(file.getOriginalFilename());
        BigDecimal sizeInMb = BigDecimal.valueOf(file.getSize())
                .divide(BigDecimal.valueOf(1_048_576), 3, java.math.RoundingMode.HALF_UP);

        AddDocumentUseCase.Command cmd = new AddDocumentUseCase.Command(
                caseId,
                file.getOriginalFilename(),
                fileType,
                file.getContentType(),
                sizeInMb,
                documentType,
                classification,
                docVersion != null ? docVersion : "1.0",
                uploaderName,
                file.getBytes()
        );

        return DocumentResponse.from(addDocumentUseCase.execute(cmd));
    }

    /** Register a document already stored in DMS without uploading content here. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a DMS document reference on a case")
    public DocumentResponse registerReference(
            @PathVariable UUID caseId,
            @RequestParam String name,
            @RequestParam(defaultValue = "SUPPORTING") CaseDocument.DocumentType documentType,
            @RequestParam(defaultValue = "INTERNAL") CaseDocument.SecurityClassification classification,
            @RequestParam(required = false) String dmsReference,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) BigDecimal sizeInMb,
            @RequestParam(required = false) String uploaderName) {

        AddDocumentUseCase.Command cmd = new AddDocumentUseCase.Command(
                caseId, name, fileType, null,
                sizeInMb != null ? sizeInMb : BigDecimal.ZERO,
                documentType, classification, "1.0", uploaderName, null);

        return DocumentResponse.from(addDocumentUseCase.execute(cmd));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
