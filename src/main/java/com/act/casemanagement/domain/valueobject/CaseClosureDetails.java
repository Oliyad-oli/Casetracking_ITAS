package com.act.casemanagement.domain.valueobject;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable record capturing all closure metadata for a Case.
 * Set once when the case transitions to CLOSED — never mutated afterwards.
 */
public record CaseClosureDetails(
    ClosureReason closureReason,
    ClosureOutcome outcome,
    String resolutionSummary,
    MonetaryAmount amountAssessed,
    MonetaryAmount amountCollected,
    MonetaryAmount amountWrittenOff,
    MonetaryAmount refundApproved,
    UUID closedById,
    String closedByName,
    Instant closedAt,
    String finalDocumentId
) {
    public enum ClosureReason {
        RESOLVED, STATUTE_BARRED, COMPLIANT, INSUFFICIENT_EVIDENCE, SETTLED, WITHDRAWN
    }

    public enum ClosureOutcome {
        ASSESSMENT_CONFIRMED, APPEAL_ALLOWED, APPEAL_DENIED,
        SETTLEMENT_REACHED, NO_ADJUSTMENT, COURT_RULING
    }
}
