package com.act.casemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ITAS Case Management Service (RCMIS) — CTR0100–CTR1100.
 * <p>
 * Hexagonal + DDD, 6-layer layout: api / application / domain / persistence /
 * engineadapter / observability / config.
 * <p>
 * No Spring Security — auth is fully owned by the API Gateway + Keycloak.
 * Actor identity is forwarded via gateway-only headers (X-Authenticated-Actor-Id,
 * X-Authenticated-Role, X-Authenticated-Unit-Id) and resolved once per request
 * into RequestActorContext. See observability/filter/RequestActorFilter.java.
 */
@SpringBootApplication
@EnableScheduling
public class CaseManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseManagementApplication.class, args);
    }
}
