package com.act.casemanagement.config;

import com.act.casemanagement.domain.service.ApprovalEscalationService;
import com.act.casemanagement.domain.service.AssignmentRecommendationService;
import com.act.casemanagement.domain.service.AutomaticTransitionService;
import com.act.casemanagement.domain.service.ReportScopeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers stateless domain services as Spring beans.
 * Domain services are pure Java — no Spring annotations inside the domain package.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public AssignmentRecommendationService assignmentRecommendationService() {
        return new AssignmentRecommendationService();
    }

    @Bean
    public ApprovalEscalationService approvalEscalationService() {
        return new ApprovalEscalationService();
    }

    @Bean
    public AutomaticTransitionService automaticTransitionService() {
        return new AutomaticTransitionService();
    }

    @Bean
    public ReportScopeService reportScopeService() {
        return new ReportScopeService();
    }
}
