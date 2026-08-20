package com.act.casemanagement.application.usecase.assignment;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.*;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.event.CaseAssigned;
import com.act.casemanagement.domain.event.CaseSelfAssigned;
import com.act.casemanagement.domain.event.DomainEvent;
import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.service.AutomaticTransitionService;
import com.act.casemanagement.domain.valueobject.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Section 5.1 proof: actor comes from RequestActorContext (header-set), not from request body.
 * Section 5.2 proof: self-assignment requires mandatory justification and emits distinct event.
 */
@ExtendWith(MockitoExtension.class)
class AssignCaseUseCaseTest {

    @Mock CaseRepositoryPort          caseRepository;
    @Mock UserRepositoryPort          userRepository;
    @Mock WorkflowStageRepositoryPort workflowStageRepository;
    @Mock EventPublisherPort          eventPublisher;

    AutomaticTransitionService transitionService = new AutomaticTransitionService();
    AssignCaseUseCase useCase;

    private static final UUID SUPERVISOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OFFICER_ID    = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        useCase = new AssignCaseUseCase(caseRepository, userRepository,
                workflowStageRepository, eventPublisher, transitionService);
    }

    @AfterEach
    void tearDown() {
        RequestActorContext.clear();
    }

    // ── Happy path: normal assignment ─────────────────────────────────────────

    @Test
    void normal_assignment_emits_CaseAssigned_actor_from_header() {
        // Section 5.1: actor resolved from RequestActorContext (header), not from DTO
        RequestActorContext.set(SUPERVISOR_ID, UserRole.SUPERVISOR, "UNIT-01");

        Case aCase = buildCase();
        when(caseRepository.findById(aCase.getId())).thenReturn(Optional.of(aCase));
        when(userRepository.findById(OFFICER_ID)).thenReturn(Optional.of(
                new UserRepositoryPort.UserRecord(OFFICER_ID, "Officer Abebe",
                        "officer@mor.gov.et", UserRole.TAX_OFFICER, "Tax Officer",
                        "Audit Division", "UNIT-01", "Addis Ababa",
                        "Audit", List.of("audit"), 5, "ACTIVE")));
        when(workflowStageRepository.findAllActive()).thenReturn(List.of());
        when(caseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AssignCaseUseCase.Command cmd = new AssignCaseUseCase.Command(
                aCase.getId(), OFFICER_ID, "Supervisor", "Good match");

        Case result = useCase.execute(cmd);

        assertThat(result.getAssignedOfficerId()).isEqualTo(OFFICER_ID);
        assertThat(result.getAssigningOfficerId()).isEqualTo(SUPERVISOR_ID); // from header!
        assertThat(result.isSelfAssigned()).isFalse();

        ArgumentCaptor<DomainEvent> cap = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, atLeastOnce()).publish(cap.capture());
        assertThat(cap.getAllValues().stream().anyMatch(e -> e instanceof CaseAssigned)).isTrue();
    }

    // ── Self-assignment: requires justification, emits distinct event ─────────

    @Test
    void self_assignment_with_justification_emits_CaseSelfAssigned() {
        // Section 5.2: actor == officerId → self-assignment path
        RequestActorContext.set(SUPERVISOR_ID, UserRole.SUPERVISOR, "UNIT-01");

        Case aCase = buildCase();
        when(caseRepository.findById(aCase.getId())).thenReturn(Optional.of(aCase));
        when(userRepository.findById(SUPERVISOR_ID)).thenReturn(Optional.of(
                new UserRepositoryPort.UserRecord(SUPERVISOR_ID, "Supervisor Mekonnen",
                        "sup@mor.gov.et", UserRole.SUPERVISOR, "Supervisor",
                        "Audit Division", "UNIT-01", "Addis Ababa",
                        "Audit", List.of(), 3, "ACTIVE")));
        when(workflowStageRepository.findAllActive()).thenReturn(List.of());
        when(caseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // officerId == actorId (SUPERVISOR_ID) → self-assignment
        AssignCaseUseCase.Command cmd = new AssignCaseUseCase.Command(
                aCase.getId(), SUPERVISOR_ID, "Supervisor", "Self-assigning due to capacity");

        Case result = useCase.execute(cmd);

        assertThat(result.isSelfAssigned()).isTrue();

        ArgumentCaptor<DomainEvent> cap = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, atLeastOnce()).publish(cap.capture());
        assertThat(cap.getAllValues().stream()
                .anyMatch(e -> e instanceof CaseSelfAssigned)).isTrue();
    }

    @Test
    void self_assignment_without_justification_throws_DomainException() {
        // Section 5.2: blank justification on self-assignment must be rejected
        RequestActorContext.set(SUPERVISOR_ID, UserRole.SUPERVISOR, "UNIT-01");

        Case aCase = buildCase();
        when(caseRepository.findById(aCase.getId())).thenReturn(Optional.of(aCase));
        // No userRepository stub needed — exception thrown before that call

        AssignCaseUseCase.Command cmd = new AssignCaseUseCase.Command(
                aCase.getId(), SUPERVISOR_ID, "Supervisor", ""); // blank reason

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("mandatory justification");
    }

    @Test
    void assign_fails_when_case_not_found() {
        RequestActorContext.set(SUPERVISOR_ID, UserRole.SUPERVISOR, "UNIT-01");
        UUID randomId = UUID.randomUUID();
        when(caseRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new AssignCaseUseCase.Command(randomId, OFFICER_ID, "Sup", "reason")))
                .hasMessageContaining("Case");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Case buildCase() {
        Case c = Case.create(UUID.randomUUID(), "ITAS-REV-2026-55555",
                "Test Assignment Case", CaseCategory.AUDIT,
                Priority.HIGH, RiskLevel.HIGH,
                new TIN("0012345678"), "Test PLC", "CORPORATE",
                SUPERVISOR_ID, "Supervisor", Instant.now().plusSeconds(86400), 45,
                MonetaryAmount.of(BigDecimal.valueOf(2_000_000)),
                "reason", "instruction", null, Map.of());
        c.pullEvents();
        return c;
    }
}
