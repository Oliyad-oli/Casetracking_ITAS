package com.act.casemanagement.application.usecase.admin;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.AssignmentWeightsRepositoryPort;
import com.act.casemanagement.domain.aggregate.AssignmentWeightsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveAssignmentWeightsUseCase {

    private final AssignmentWeightsRepositoryPort weightsRepository;

    @Transactional
    public AssignmentWeightsConfig execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        AssignmentWeightsConfig config = AssignmentWeightsConfig.create(
                UUID.randomUUID(),
                cmd.capacityWeight(),
                cmd.specializationWeight(),
                cmd.regionWeight(),
                cmd.caseloadMixWeight(),
                Instant.now(),
                actor.getActorId()
        );
        return weightsRepository.save(config);
    }

    public record Command(
            BigDecimal capacityWeight,
            BigDecimal specializationWeight,
            BigDecimal regionWeight,
            BigDecimal caseloadMixWeight
    ) {}
}
