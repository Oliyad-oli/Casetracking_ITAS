package com.act.casemanagement.application.usecase.cases;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.application.port.EventPublisherPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.event.CaseCreated;
import com.act.casemanagement.domain.event.DomainEvent;
import com.act.casemanagement.domain.exception.DomainException;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for CreateCaseUseCase.
 * No Spring context — @ExtendWith(MockitoExtension.class) only.
 * Uses real Case domain instances (never mock AggregateRoot subclasses).
 */
@ExtendWith(MockitoExtension.class)
class CreateCaseUseCaseTest {

    @Mock CaseRepositoryPort caseRepository;
    @Mock EventPublisherPort  eventPublisher;

    CreateCaseUseCase useCase;

    private static final UUID ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        useCase = new CreateCaseUseCase(caseRepository, eventPublisher);
        RequestActorContext.set(ACTOR_ID, UserRole.SUPERVISOR, "UNIT-01");
    }

    @AfterEach
    void tearDown() {
        RequestActorContext.clear();
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void execute_creates_case_saves_and_publishes_CaseCreated() {
        CreateCaseUseCase.Command cmd = validCommand();

        // Repository returns the same aggregate (simulates save)
        when(caseRepository.save(any(Case.class))).thenAnswer(inv -> {
            Case c = inv.getArgument(0);
            c.setVersion(0L);
            return c;
        });

        Case result = useCase.execute(cmd);

        // State assertions
        assertThat(result.getCategory()).isEqualTo(CaseCategory.AUDIT);
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(result.getTin().value()).isEqualTo("0012345678");
        assertThat(result.getStatus()).isEqualTo(CaseStatus.UNASSIGNED);
        assertThat(result.getCreatedById()).isEqualTo(ACTOR_ID);

        // Event assertions — must be called on SAVED aggregate
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, atLeastOnce()).publish(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues().stream()
                .anyMatch(e -> e instanceof CaseCreated)).isTrue();
    }

    @Test
    void execute_sets_actor_from_context_not_from_body() {
        CreateCaseUseCase.Command cmd = validCommand();
        when(caseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Case result = useCase.execute(cmd);

        // Actor must come from RequestActorContext, not any DTO field
        assertThat(result.getCreatedById()).isEqualTo(ACTOR_ID);
    }

    // ── Rejection path ────────────────────────────────────────────────────────

    @Test
    void execute_rejects_invalid_tin() {
        CreateCaseUseCase.Command cmd = new CreateCaseUseCase.Command(
                "Title", CaseCategory.AUDIT, Priority.HIGH, RiskLevel.HIGH,
                "123",   // invalid TIN — not 10 digits
                "ABC PLC", "CORPORATE", "Actor", Instant.now().plusSeconds(86400),
                45, BigDecimal.valueOf(1_000_000), "reasons", "instructions", null, Map.of());

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("TIN");
    }

    @Test
    void execute_uses_default_targetDays_when_zero() {
        CreateCaseUseCase.Command cmd = new CreateCaseUseCase.Command(
                "Title", CaseCategory.AUDIT, Priority.MEDIUM, RiskLevel.LOW,
                "0012345678", "ABC PLC", "CORPORATE", "Actor",
                Instant.now().plusSeconds(86400),
                0, // targetDays = 0 → should default to 45
                BigDecimal.valueOf(0), "reasons", "instructions", null, Map.of());

        when(caseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Case result = useCase.execute(cmd);
        assertThat(result.getTargetDays()).isEqualTo(45);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private CreateCaseUseCase.Command validCommand() {
        return new CreateCaseUseCase.Command(
                "Audit Case — ABC Manufacturing",
                CaseCategory.AUDIT, Priority.HIGH, RiskLevel.HIGH,
                "0012345678", "ABC Manufacturing PLC", "CORPORATE",
                "Supervisor Mekonnen", Instant.now().plusSeconds(2_592_000),
                45, BigDecimal.valueOf(5_000_000),
                "VAT discrepancy", "Request all records", "None", Map.of());
    }
}
