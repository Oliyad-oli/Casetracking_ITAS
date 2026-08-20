package com.act.casemanagement.config;

import com.act.casemanagement.application.port.UserRepositoryPort.UserRecord;
import com.act.casemanagement.domain.valueobject.UserRole;
import com.act.casemanagement.engineadapter.userprojection.UserProjectionAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.UUID;

/**
 * Seeds the in-memory user projection with a representative set of officers.
 * Active on all profiles except "test" — integration tests supply their own data.
 *
 * Replace with a DB-backed projection feed from registration-service when available.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserProjectionSeedConfig {

    private final UserProjectionAdapter userProjection;

    @Bean
    @Profile("!test")
    public ApplicationRunner seedUsers() {
        return args -> {
            List<UserRecord> users = List.of(
                new UserRecord(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Supervisor Mekonnen", "supervisor@mor.gov.et",
                    UserRole.SUPERVISOR, "Tax Supervisor",
                    "Audit Division", "UNIT-01", "Addis Ababa Central",
                    "Audit", List.of("audit", "compliance"), 5, "ACTIVE"),
                new UserRecord(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    "Officer Abebe", "officer@mor.gov.et",
                    UserRole.TAX_OFFICER, "Senior Tax Officer",
                    "Audit Division", "UNIT-01", "Addis Ababa Central",
                    "Audit", List.of("audit", "vat"), 8, "ACTIVE"),
                new UserRecord(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                    "Manager Tigist", "manager@mor.gov.et",
                    UserRole.MANAGER, "Tax Manager",
                    "Revenue Operations", "UNIT-02", "Addis Ababa North",
                    "Management", List.of("compliance", "debt"), 3, "ACTIVE"),
                new UserRecord(UUID.fromString("00000000-0000-0000-0000-000000000004"),
                    "Senior Manager Dawit", "senior.manager@mor.gov.et",
                    UserRole.SENIOR_MANAGER, "Senior Director",
                    "Legal Affairs", "UNIT-03", "Headquarters",
                    "Legal", List.of("litigation", "objection"), 2, "ACTIVE"),
                new UserRecord(UUID.fromString("00000000-0000-0000-0000-000000000005"),
                    "Commissioner Almaz", "commissioner@mor.gov.et",
                    UserRole.COMMISSIONER, "Tax Commissioner",
                    "Executive", "UNIT-00", "Headquarters",
                    "Executive", List.of(), 1, "ACTIVE")
            );
            users.forEach(userProjection::seed);
            log.info("UserProjectionSeedConfig: seeded {} users", users.size());
        };
    }
}
