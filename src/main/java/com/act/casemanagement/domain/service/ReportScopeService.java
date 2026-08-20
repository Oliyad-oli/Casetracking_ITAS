package com.act.casemanagement.domain.service;

import com.act.casemanagement.domain.valueobject.UserRole;

import java.util.UUID;

/**
 * Domain service — ports exact report-scope logic from the frontend's
 * {@code reportScope.ts}.
 * <p>
 * <ul>
 *   <li>TAX_OFFICER  → personal scope: only their own assigned cases</li>
 *   <li>SUPERVISOR   → team/unit scope: cases created by, assigned by, or belonging to
 *                      officers in the same unit</li>
 *   <li>MANAGER and above → organisation-wide</li>
 * </ul>
 *
 * The scope is applied in the use case layer using the trusted actor from
 * RequestActorContext — never from a client-supplied filter parameter.
 */
public class ReportScopeService {

    public enum ScopeType { PERSONAL, TEAM, ORGANIZATION }

    public record ScopeDescriptor(
        ScopeType scopeType,
        String scopeLabel,
        boolean isPersonalOnly,
        boolean canViewColleagues
    ) {}

    /**
     * Derives the scope descriptor for the given actor. The use case then
     * applies this to filter its repository query accordingly.
     */
    public ScopeDescriptor deriveScope(UUID actorId, UserRole role, String unitId) {
        return switch (role) {
            case TAX_OFFICER -> new ScopeDescriptor(
                ScopeType.PERSONAL,
                "Your Cases (Personal Scope)",
                true,
                false
            );
            case SUPERVISOR -> new ScopeDescriptor(
                ScopeType.TEAM,
                "Unit Portfolio",
                false,
                true
            );
            default -> new ScopeDescriptor(
                ScopeType.ORGANIZATION,
                "Organisation-Wide Portfolio",
                false,
                true
            );
        };
    }

    /** Returns true if this actor can see cases in the given unit. */
    public boolean canViewUnit(UUID actorId, UserRole role, String actorUnitId,
                               String targetUnitId) {
        return switch (role) {
            case TAX_OFFICER -> false; // personal only
            case SUPERVISOR  -> actorUnitId != null && actorUnitId.equals(targetUnitId);
            default          -> true;  // manager and above: org-wide
        };
    }

    /** Returns true if this actor can view another officer's personal cases. */
    public boolean canViewOfficerCases(UUID actorId, UserRole role,
                                       String actorUnitId,
                                       UUID targetOfficerId, String targetUnitId) {
        if (actorId.equals(targetOfficerId)) return true;
        return switch (role) {
            case TAX_OFFICER -> false;
            case SUPERVISOR  -> actorUnitId != null && actorUnitId.equals(targetUnitId);
            default          -> true;
        };
    }
}
