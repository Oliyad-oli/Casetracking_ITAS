package com.act.casemanagement.domain.model;

import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.valueobject.NoteEditHistoryEntry;
import com.act.casemanagement.domain.valueobject.UserRole;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Case note entity — child of the Case aggregate.
 * <p>
 * Immutability invariant (CTR0500): {@code originalContent} is set exactly
 * once at creation and NEVER changed again. Every subsequent edit appends
 * a {@link NoteEditHistoryEntry} carrying the full before/after text.
 * <p>
 * Version is carried from the persistence layer on load and must be written
 * back on save to prevent the Hibernate optimistic-lock "different object"
 * error.
 */
@Getter
public class CaseNote {

    private final UUID id;
    private final UUID caseId;
    private final String originalContent;   // Set once at creation — immutable
    private String content;                 // Current content
    private final LocalDate eventDate;
    private final Instant recordingDate;
    private final UUID authorId;
    private final String authorName;
    private final UserRole authorRole;
    private boolean obsolete;
    private String obsoleteReason;
    private boolean edited;
    private final List<NoteEditHistoryEntry> editHistory;
    private String attachmentName;
    private Long version;                   // JPA @Version — must round-trip

    /** Factory — called by CreateCaseNoteUseCase. */
    public static CaseNote create(
            UUID id,
            UUID caseId,
            String content,
            LocalDate eventDate,
            Instant recordingDate,
            UUID authorId,
            String authorName,
            UserRole authorRole,
            String attachmentName) {
        if (content == null || content.isBlank()) {
            throw new DomainException("Note content must not be blank");
        }
        CaseNote note = new CaseNote(id, caseId, content, eventDate, recordingDate,
                authorId, authorName, authorRole, attachmentName);
        return note;
    }

    /** Reconstitution from persistence. */
    public static CaseNote reconstitute(
            UUID id, UUID caseId, String originalContent, String content,
            LocalDate eventDate, Instant recordingDate,
            UUID authorId, String authorName, UserRole authorRole,
            boolean obsolete, String obsoleteReason,
            boolean edited, List<NoteEditHistoryEntry> editHistory,
            String attachmentName, Long version) {
        CaseNote note = new CaseNote(id, caseId, originalContent, eventDate,
                recordingDate, authorId, authorName, authorRole, attachmentName);
        note.content        = content;
        note.obsolete       = obsolete;
        note.obsoleteReason = obsoleteReason;
        note.edited         = edited;
        note.editHistory.addAll(editHistory != null ? editHistory : List.of());
        note.version        = version;
        return note;
    }

    private CaseNote(UUID id, UUID caseId, String content, LocalDate eventDate,
                     Instant recordingDate, UUID authorId, String authorName,
                     UserRole authorRole, String attachmentName) {
        this.id              = id;
        this.caseId          = caseId;
        this.originalContent = content;
        this.content         = content;
        this.eventDate       = eventDate;
        this.recordingDate   = recordingDate;
        this.authorId        = authorId;
        this.authorName      = authorName;
        this.authorRole      = authorRole;
        this.attachmentName  = attachmentName;
        this.editHistory     = new ArrayList<>();
        this.obsolete        = false;
        this.edited          = false;
    }

    /**
     * Edit the note content. Appends an edit-history entry carrying the full
     * before/after text — never replacing originalContent.
     */
    public void edit(String newContent, UUID editorId, String editorName, String reason) {
        if (obsolete) {
            throw new DomainException("Cannot edit an obsolete note (id=" + id + ")");
        }
        if (newContent == null || newContent.isBlank()) {
            throw new DomainException("New note content must not be blank");
        }
        editHistory.add(new NoteEditHistoryEntry(
                this.content, newContent, Instant.now(), editorId, editorName, reason));
        this.content = newContent;
        this.edited  = true;
    }

    /** Mark note obsolete. Cannot be undone. */
    public void markObsolete(String reason) {
        if (obsolete) {
            throw new DomainException("Note is already obsolete (id=" + id + ")");
        }
        this.obsolete       = true;
        this.obsoleteReason = reason;
    }

    public List<NoteEditHistoryEntry> getEditHistory() {
        return Collections.unmodifiableList(editHistory);
    }

    public void setVersion(Long version) { this.version = version; }
}
