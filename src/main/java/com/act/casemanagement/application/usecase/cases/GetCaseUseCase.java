package com.act.casemanagement.application.usecase.cases;

import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCaseUseCase {

    private final CaseRepositoryPort caseRepository;

    @Transactional(readOnly = true)
    public Case execute(UUID caseId) {
        return caseRepository.findById(caseId)
            .orElseThrow(() -> new ResourceNotFoundException("Case", caseId));
    }
}
