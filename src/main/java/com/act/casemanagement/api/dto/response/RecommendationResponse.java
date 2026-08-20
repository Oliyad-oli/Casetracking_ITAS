package com.act.casemanagement.api.dto.response;

import com.act.casemanagement.domain.service.AssignmentRecommendationService.RecommendationScore;

import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
        UUID officerId, String officerName, int totalScore,
        int capacityScore, int specializationScore, int regionScore, int caseloadMixScore,
        int activeCaseCount, int utilizationPct, String specializationLabel,
        List<String> reasons, boolean isOverCapacity, boolean isSelfAssignment
) {
    public static RecommendationResponse from(RecommendationScore s) {
        return new RecommendationResponse(s.officerId(), s.officerName(), s.totalScore(),
                s.capacityScore(), s.specializationScore(), s.regionScore(), s.caseloadMixScore(),
                s.activeCaseCount(), s.utilizationPct(), s.specializationLabel(),
                s.reasons(), s.isOverCapacity(), s.isSelfAssignment());
    }
}
