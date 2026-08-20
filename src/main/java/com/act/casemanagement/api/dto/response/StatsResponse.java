package com.act.casemanagement.api.dto.response;

import com.act.casemanagement.application.usecase.stats.GetActivityStatisticsUseCase;

import java.util.Map;

public record StatsResponse(
        long totalCases, long openCases, long closedCases,
        long unassignedCases, long criticalCases, long pendingApproval,
        Map<String, Long> byCategory, Map<String, Long> byStatus,
        String scopeType, String scopeLabel
) {
    public static StatsResponse from(GetActivityStatisticsUseCase.Stats s) {
        return new StatsResponse(s.totalCases(), s.openCases(), s.closedCases(),
                s.unassignedCases(), s.criticalCases(), s.pendingApproval(),
                s.byCategory(), s.byStatus(), s.scopeType(), s.scopeLabel());
    }
}
