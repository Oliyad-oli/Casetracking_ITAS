package com.act.casemanagement.application.usecase.assignment;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.AssignmentWeightsRepositoryPort;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.application.port.UserRepositoryPort;
import com.act.casemanagement.domain.aggregate.AssignmentWeightsConfig;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import com.act.casemanagement.domain.service.AssignmentRecommendationService;
import com.act.casemanagement.domain.service.AssignmentRecommendationService.OfficerSnapshot;
import com.act.casemanagement.domain.service.AssignmentRecommendationService.RecommendationScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAssignmentRecommendationsUseCase {

    private final CaseRepositoryPort                caseRepository;
    private final UserRepositoryPort                userRepository;
    private final AssignmentWeightsRepositoryPort   weightsRepository;
    private final AssignmentRecommendationService   recommendationService;

    @Transactional(readOnly = true)
    public List<RecommendationScore> execute(UUID caseId) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId));

        AssignmentWeightsConfig weights = weightsRepository.findActive()
                .orElseGet(this::defaultWeights);

        List<OfficerSnapshot> officers = userRepository.findEligibleOfficers().stream()
                .map(u -> new OfficerSnapshot(
                        u.id(), u.name(), u.unit(), u.department(),
                        u.title(), u.region(), u.specializations(),
                        u.role().name(),
                        (int) caseRepository.countActiveByOfficerId(u.id()),
                        (int) caseRepository.countHighRiskActiveByOfficerId(u.id())))
                .toList();

        return recommendationService.recommend(
                aCase.getCategory(),
                aCase.getPriority(),
                aCase.getEstimatedAmountEtb().value(),
                officers,
                actor.getActorId(),
                weights);
    }

    private AssignmentWeightsConfig defaultWeights() {
        return AssignmentWeightsConfig.create(
                UUID.randomUUID(),
                new BigDecimal("0.35"), new BigDecimal("0.35"),
                new BigDecimal("0.15"), new BigDecimal("0.15"),
                java.time.Instant.now(), null);
    }
}
