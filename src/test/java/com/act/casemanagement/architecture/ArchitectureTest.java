package com.act.casemanagement.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Enforces 6 mandatory architecture rules for case-backend-core-server.
 * Must always be green — mvn test -Dtest=ArchitectureTest.
 *
 * Rules:
 *   1. Layered dependency rule (api → application → domain ← persistence/engineadapter)
 *   2. Domain isolation (no JPA, Spring, persistence, api, engineadapter in domain)
 *   3. No API layer calling engineadapter directly
 *   4. Persistence classes outside adapter must not depend on application
 *   5. No Spring Security anywhere (platform convention — gateway owns auth)
 *   6. Adapters must not carry @Transactional (use case owns the transaction)
 */
class ArchitectureTest {

    private static final String BASE = "com.act.casemanagement";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    // ── Rule 1: Layered dependency ──────────────────────────────────────────

    @Test
    void layered_dependency_rule() {
        ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Api")           .definedBy(BASE + ".api..")
                .layer("Application")   .definedBy(BASE + ".application..")
                .layer("Domain")        .definedBy(BASE + ".domain..")
                .layer("Persistence")   .definedBy(BASE + ".persistence..")
                .layer("EngineAdapter") .definedBy(BASE + ".engineadapter..")
                .layer("Observability") .definedBy(BASE + ".observability..")
                .layer("Config")        .definedBy(BASE + ".config..")

                .whereLayer("Api")          .mayOnlyBeAccessedByLayers("Config")
                .whereLayer("Application")  .mayOnlyBeAccessedByLayers("Api", "Config", "Observability")
                .whereLayer("Domain")       .mayOnlyBeAccessedByLayers(
                        "Api", "Application", "Persistence", "EngineAdapter", "Config", "Observability")
                .whereLayer("Persistence")  .mayOnlyBeAccessedByLayers("Config")
                .whereLayer("EngineAdapter").mayOnlyBeAccessedByLayers("Config")
                .whereLayer("Observability").mayOnlyBeAccessedByLayers("Config");

        rule.check(classes);
    }

    // ── Rule 2: Domain isolation ────────────────────────────────────────────

    @Test
    void domain_must_not_depend_on_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.springframework..",
                        BASE + ".persistence..",
                        BASE + ".api..",
                        BASE + ".engineadapter.."
                )
                .because("The domain layer must be infrastructure-free");

        rule.check(classes);
    }

    // ── Rule 3: No API → EngineAdapter direct calls ─────────────────────────

    @Test
    void api_must_not_call_engineadapter_directly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE + ".api..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE + ".engineadapter..")
                .because("API layer must go through application use cases, not adapters");

        rule.check(classes);
    }

    // ── Rule 4: Persistence entities/repos must not depend on application ───

    @Test
    void persistence_jpa_must_not_depend_on_application() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        BASE + ".persistence.jpa.entity..",
                        BASE + ".persistence.jpa.repository.."
                )
                .should().dependOnClassesThat()
                .resideInAPackage(BASE + ".application..")
                .because("JPA entities and repos are infrastructure — they must not depend on application");

        rule.check(classes);
    }

    // ── Rule 5: No Spring Security anywhere (Section 5.1 / AGENTS.md §3.9) ──

    @Test
    void no_spring_security_anywhere() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE + "..")
                .should().dependOnClassesThat()
                .resideInAPackage("org.springframework.security..")
                .because("Security is owned by the API Gateway + Keycloak. " +
                         "Do NOT add spring-boot-starter-security or any Spring Security import.");

        rule.check(classes);
    }

    // ── Rule 6: Persistence adapters must not carry @Transactional ──────────

    @Test
    void persistence_adapters_must_not_be_transactional() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE + ".persistence.adapter..")
                .should().beAnnotatedWith(
                        org.springframework.transaction.annotation.Transactional.class)
                .because("The use case owns the transaction boundary — adapters must not carry @Transactional");

        rule.check(classes);
    }
}
