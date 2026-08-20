package com.act.casemanagement.application.port;

import com.act.casemanagement.domain.valueobject.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-only projection of user data — registration-service owns the write side.
 * Feeds into AssignmentRecommendationService and ReportScopeService.
 */
public interface UserRepositoryPort {
    Optional<UserRecord> findById(UUID userId);
    List<UserRecord> findByRole(UserRole role);
    List<UserRecord> findByUnitId(String unitId);
    List<UserRecord> findEligibleOfficers(); // TAX_OFFICER + SUPERVISOR

    record UserRecord(
        UUID id,
        String name,
        String email,
        UserRole role,
        String title,
        String unit,
        String unitId,
        String region,
        String department,
        List<String> specializations,
        int activeCasesCount,
        String status
    ) {}
}
