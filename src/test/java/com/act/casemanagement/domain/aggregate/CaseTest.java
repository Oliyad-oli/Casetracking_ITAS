package com.act.casemanagement.domain.aggregate;

import com.act.casemanagement.domain.event.*;
import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Case aggregate root.
 * Uses real domain instances — never Mockito.mock(AggregateRoot subclass)
 * (final equals/hashCode causes issues on newer JVMs).
 */
class CaseTest {

    private static final UUID ACTOR_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OFFICER_A = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID OFFICER_B = UUID.fromString("00000000-0000-0000-0000-000000000003");

    // ── Factory ───────────────────────────────────────────────────────────────

    @Test
    void create_emits_CaseCreated_event() {
        Case c = buildCase();
        List<DomainEvent> events = c.pullEvents();

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(CaseCreated.class);
        CaseCreated ev = (CaseCreated) events.get(0);
        assertThat(ev.tin()).isEqualTo("0012345678");
        assertThat(ev.category()).isEqualTo(CaseCategory.AUDIT);
    }

    @Test
    void pullEvents_drains_list() {
        Case c = buildCase();
        c.pullEvents();
        assertThat(c.pullEvents()).isEmpty();
    }

    // ── Assignment ────────────────────────────────────────────────────────────

    @Test
    void assign_emits_CaseAssigned_and_moves_to_ASSIGNED() {
        Case c = buildCase();
        c.pullEvents(); // drain create event

        c.assign(OFFICER_A, "Officer Abebe", "UNIT-01", "Audit",
                ACTOR_ID, "Supervisor", "Good audit match");

        assertThat(c.getStatus()).isEqualTo(CaseStatus.ASSIGNED);
        assertThat(c.getAssignedOfficerId()).isEqualTo(OFFICER_A);
        List<DomainEvent> events = c.pullEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(CaseAssigned.class);
    }

    @Test
    void self_assign_emits_CaseSelfAssigned_with_justification() {
        Case c = buildCase();
        c.pullEvents();

        // actorId == officerId → self-assignment
        c.assign(ACTOR_ID, "Supervisor", "UNIT-01", "Audit",
                ACTOR_ID, "Supervisor", "Self-assigning due to urgent escalation");

        List<DomainEvent> events = c.pullEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(CaseSelfAssigned.class);
        assertThat(c.isSelfAssigned()).isTrue();
    }

    @Test
    void self_assign_without_justification_throws() {
        Case c = buildCase();
        c.pullEvents();

        assertThatThrownBy(() ->
                c.assign(ACTOR_ID, "Supervisor", "UNIT-01", "Audit",
                        ACTOR_ID, "Supervisor", ""))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("mandatory justification");
    }

    @Test
    void deassign_moves_to_UNASSIGNED_and_emits_event() {
        Case c = buildCase();
        c.pullEvents();
        c.assign(OFFICER_A, "Officer", "UNIT-01", "Audit",
                ACTOR_ID, "Supervisor", "Normal assignment");
        c.pullEvents();

        c.deassign(ACTOR_ID, "Reassignment required");

        assertThat(c.getStatus()).isEqualTo(CaseStatus.UNASSIGNED);
        assertThat(c.getAssignedOfficerId()).isNull();
        assertThat(c.pullEvents()).hasSize(1);
        assertThat(c.pullEvents().get(0)).isInstanceOf(CaseDeassigned.class);
    }

    // ── Notes — immutability invariant ───────────────────────────────────────

    @Test
    void addNote_sets_originalContent_immutably() {
        Case c = buildCase();
        c.pullEvents();

        c.addNote(UUID.randomUUID(), "Initial content", LocalDate.now(),
                ACTOR_ID, "Officer", UserRole.TAX_OFFICER, null);

        var note = c.getNotes().get(0);
        assertThat(note.getOriginalContent()).isEqualTo("Initial content");
        assertThat(note.getContent()).isEqualTo("Initial content");
    }

    @Test
    void editNote_appends_to_history_never_changes_originalContent() {
        Case c = buildCase();
        c.pullEvents();
        UUID noteId = UUID.randomUUID();
        c.addNote(noteId, "First draft", LocalDate.now(),
                ACTOR_ID, "Officer", UserRole.TAX_OFFICER, null);
        c.pullEvents();

        c.editNote(noteId, "Revised content", ACTOR_ID, "Officer", "Typo fix");
        c.editNote(noteId, "Final content",   ACTOR_ID, "Officer", "Clarity update");

        var note = c.findNote(noteId);
        assertThat(note.getOriginalContent()).isEqualTo("First draft");   // NEVER changes
        assertThat(note.getContent()).isEqualTo("Final content");
        assertThat(note.getEditHistory()).hasSize(2);
        assertThat(note.getEditHistory().get(0).previousContent()).isEqualTo("First draft");
        assertThat(note.getEditHistory().get(0).newContent()).isEqualTo("Revised content");
        assertThat(note.getEditHistory().get(1).previousContent()).isEqualTo("Revised content");

        List<DomainEvent> events = c.pullEvents();
        // Two CaseNoteEdited events carrying actual before/after text
        assertThat(events.stream().filter(e -> e instanceof CaseNoteEdited).count()).isEqualTo(2);
        CaseNoteEdited firstEdit = events.stream()
                .filter(e -> e instanceof CaseNoteEdited)
                .map(e -> (CaseNoteEdited) e).findFirst().orElseThrow();
        assertThat(firstEdit.previousContent()).isEqualTo("First draft");
        assertThat(firstEdit.newContent()).isEqualTo("Revised content");
    }

    @Test
    void markNoteObsolete_blocks_subsequent_edits() {
        Case c = buildCase();
        c.pullEvents();
        UUID noteId = UUID.randomUUID();
        c.addNote(noteId, "Some note", LocalDate.now(),
                ACTOR_ID, "Officer", UserRole.TAX_OFFICER, null);

        c.markNoteObsolete(noteId, ACTOR_ID, "No longer relevant");

        var note = c.findNote(noteId);
        assertThat(note.isObsolete()).isTrue();
        assertThatThrownBy(() ->
                c.editNote(noteId, "try to edit", ACTOR_ID, "Officer", "reason"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("obsolete");
    }

    // ── Closure guard ────────────────────────────────────────────────────────

    @Test
    void operations_on_closed_case_throw_DomainException() {
        Case c = buildCase();
        c.pullEvents();
        c.assign(ACTOR_ID, "Supervisor", "UNIT-01", "Audit",
                ACTOR_ID, "Supervisor", "Self-assign for closure");
        c.pullEvents();

        CaseClosureDetails closure = new CaseClosureDetails(
                CaseClosureDetails.ClosureReason.RESOLVED,
                CaseClosureDetails.ClosureOutcome.ASSESSMENT_CONFIRMED,
                "Resolved", MonetaryAmount.zero(), MonetaryAmount.zero(),
                MonetaryAmount.zero(), MonetaryAmount.zero(),
                ACTOR_ID, "Supervisor", Instant.now(), null);
        c.close(closure);
        c.pullEvents();

        assertThatThrownBy(() ->
                c.addNote(UUID.randomUUID(), "post-close note", LocalDate.now(),
                        ACTOR_ID, "Officer", UserRole.TAX_OFFICER, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("CLOSED");

        assertThatThrownBy(() ->
                c.assign(OFFICER_A, "Officer", "UNIT-01", "Audit",
                        ACTOR_ID, "Supervisor", "reason"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void closing_already_closed_case_throws() {
        Case c = buildCase();
        c.pullEvents();
        CaseClosureDetails closure = new CaseClosureDetails(
                CaseClosureDetails.ClosureReason.RESOLVED,
                CaseClosureDetails.ClosureOutcome.NO_ADJUSTMENT,
                "Done", MonetaryAmount.zero(), MonetaryAmount.zero(),
                MonetaryAmount.zero(), MonetaryAmount.zero(),
                ACTOR_ID, "Admin", Instant.now(), null);
        c.close(closure);
        c.pullEvents();

        assertThatThrownBy(() -> c.close(closure))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already closed");
    }

    // ── AggregateRoot equality ────────────────────────────────────────────────

    @Test
    void equality_is_identity_based() {
        UUID id = UUID.randomUUID();
        Case c1 = buildCaseWithId(id);
        Case c2 = buildCaseWithId(id);
        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Case buildCase() {
        return buildCaseWithId(UUID.randomUUID());
    }

    private Case buildCaseWithId(UUID id) {
        return Case.create(id, "ITAS-REV-2026-10001", "Test Audit Case",
                CaseCategory.AUDIT, Priority.HIGH, RiskLevel.HIGH,
                new TIN("0012345678"), "ABC PLC", "CORPORATE",
                ACTOR_ID, "Supervisor", Instant.now().plusSeconds(2592000), 45,
                MonetaryAmount.of(BigDecimal.valueOf(5_000_000)),
                "Test reason", "Test instruction", null, Map.of());
    }
}
