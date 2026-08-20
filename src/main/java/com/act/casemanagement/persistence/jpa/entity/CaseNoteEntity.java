package com.act.casemanagement.persistence.jpa.entity;

import com.act.casemanagement.persistence.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "case_notes")
@Getter @Setter
public class CaseNoteEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "original_content", nullable = false, columnDefinition = "TEXT")
    private String originalContent;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "recording_date", nullable = false)
    private Instant recordingDate;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_name", nullable = false, length = 200)
    private String authorName;

    @Column(name = "author_role", nullable = false, length = 60)
    private String authorRole;

    @Column(name = "is_obsolete", nullable = false)
    private boolean obsolete;

    @Column(name = "obsolete_reason", columnDefinition = "TEXT")
    private String obsoleteReason;

    @Column(name = "is_edited", nullable = false)
    private boolean edited;

    /** Stores List<NoteEditHistoryEntry> as JSONB. */
    @Convert(converter = JsonbConverter.class)
    @Column(name = "edit_history", columnDefinition = "JSONB")
    private Map<String, Object> editHistory;   // stored as JSON array via generic map

    @Column(name = "attachment_name", length = 300)
    private String attachmentName;
}
