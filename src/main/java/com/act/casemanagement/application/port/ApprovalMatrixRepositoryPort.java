package com.act.casemanagement.application.port;

import com.act.casemanagement.domain.aggregate.ApprovalMatrixConfig;

import java.util.List;
import java.util.UUID;

public interface ApprovalMatrixRepositoryPort {
    List<ApprovalMatrixConfig> findAllActive();
    ApprovalMatrixConfig save(ApprovalMatrixConfig config);
    void saveAll(List<ApprovalMatrixConfig> configs);
}
