# case-backend-core-server — Agent Instructions

This file is the canonical "how to work in this repo" guide for any AI agent.
Read it before making changes. Equivalent rules apply at the org level —
see `bs-filing-core-server/AGENTS.md` and its siblings.

---

## 1. What this service does

RCMIS Case Management Service. Implements CTR0100–CTR1100 (Case Tracking and
Management requirements). Owns the full case lifecycle: creation, assignment,
workflow, approval chain, note management, task reminders, document tracking,
case closure, and cross-case associations.

---

## 2. Build / run / test

- **Java 21** required.
- Run all tests: `mvn test`
- Run integration tests: `mvn verify`
- Start locally: `mvn spring-boot:run` — listens on **8082**.
- IT tests are skipped gracefully when Docker is not running.
- ArchUnit rules: `mvn test -Dtest=ArchitectureTest` — MANDATORY, must be green.

---

## 3. Architecture rules (non-negotiable)

### 3.1 Six-layer hexagonal layout

```
api/  →  application/  →  domain/  ←  persistence/, engineadapter/
                          ↑
                       (no inbound deps)
```

### 3.2 Actor identity — HEADER-BASED ONLY (Section 5.1 fix)

**NEVER** read actor identity from a request body field. The API gateway
(Keycloak) forwards the authenticated identity via:
- `X-Authenticated-Actor-Id`  — the actor's UUID
- `X-Authenticated-Role`       — the actor's role (TAX_OFFICER, SUPERVISOR, etc.)
- `X-Authenticated-Unit-Id`    — the actor's organisational unit UUID

`RequestActorFilter` reads these headers once per request and populates
`RequestActorContext` (a `ThreadLocal`-backed bean). Controllers and use cases
read the actor from `RequestActorContext`, **never** from request DTOs.
Request DTOs must not carry actor identity fields at all.

ArchUnit rule `no_spring_security_anywhere` is still enforced — no Spring
Security dependency anywhere.

### 3.3 Per-record authorization belongs in the use case layer (Section 5.2 fix)

Gateway RBAC (is this a valid token for role X?) is the gateway's job.
Record-level authorization (is this specific user the assigning officer of
this specific case?) belongs in the use case, using the trusted actor from
`RequestActorContext`. Throw `ForbiddenOperationException` (a domain exception)
rather than relying on any Spring construct. Log all rejections as FAILURE
audit entries.

### 3.4 Aggregates + events (DDD)

- Every write use case ends with `pullEvents().forEach(eventPublisher::publish)`
  after `save()`.
- Aggregates guard their own state transitions and throw `DomainException` on
  invalid transitions.
- Value objects are Java records in `domain/valueobject/` — standalone
  top-level types only, never nested inside aggregates.
- Domain methods register events; the use case drains them.

### 3.5 Outbox (Rule 12 equivalent)

External integrations (notifications, filing-service correlation) go through
the outbox: enqueue an `OutboxEntry` inside the same transaction as the domain
change; a separate scheduled `OutboxDispatcherService` drains PENDING entries
with exponential backoff.

### 3.6 Resilience

Every `engineadapter` method has `@CircuitBreaker(name = "...", fallbackMethod
= "...")` and `@Retry(name = "...")`. Fallback throws `wrapException(...)` from
`BaseEngineAdapter`.

### 3.7 CaseNote immutability invariant

`CaseNote.originalContent` is set **once** at creation, never changed again.
Every subsequent edit appends a `NoteEditHistoryEntry` to `editHistory`. The
audit log for an edit carries actual before/after text.

### 3.8 Audit log is append-only

`AuditLogPersistenceAdapter` exposes only `save()` — no update or delete.
The DB table has no `UPDATE`/`DELETE` grants at the application level.

### 3.9 No Spring Security anywhere

ArchUnit rule `no_spring_security_anywhere` enforces this. Do not add
`spring-boot-starter-security` or any `org.springframework.security` import.

---

## 4. Persistence rules

- Never edit a committed Flyway migration. Always add `VNN__describe_change.sql`.
- Hibernate: `ddl-auto: validate`.
- `@Version` on entities: carry the persisted version into domain and back on
  save to avoid the "different object with same identifier" optimistic lock error.
- Persistence adapters do NOT carry `@Transactional` — use case owns it.
- JSONB columns: `@Convert(converter = JsonbConverter.class)`.

---

## 5. API rules

- All endpoints under `/api/v1/...`.
- No auth annotations anywhere.
- Request DTOs own `toDomain(actorContext)`; response DTOs own
  `static from(DomainObject)`.
- Controllers are pure orchestration — no business logic.
- Errors via RFC 7807 `ProblemDetail`: domain rule → 422, forbidden → 403,
  not found → 404, validation → 400, engine failure → 503.
- `@Tag` on every controller class with CTR requirement id.
- `@Operation` on every endpoint.

---

## 6. Testing rules

- One unit test per use case. `@ExtendWith(MockitoExtension.class)`, no Spring context.
- Cover happy path + at least one rejection path + one authorization check.
- Use real domain instances in tests — do NOT `Mockito.mock(AggregateRoot subclass)`.
  (final equals/hashCode causes issues on newer JVMs.)
- IT tests use Testcontainers PostgreSQL; skip gracefully without Docker.
- ArchUnit test enforces 6 rules (including `no_spring_security_anywhere`).

---

## 7. Bruno API collections

- Collections live in `bruno/`.
- Update Bruno requests when an endpoint changes.
- Environment in `bruno/environments/local.bru` — `baseUrl` defaults to
  `http://localhost:8082`.

---

## 8. Cross-service integration note (CTR section 5.4)

`bs-filing-core-server` already has a `CasesController` + `CaseManagementPort`
(`openFraudCase`, `openCaseFromError`, `listCategoriesFor`). Error-driven cases
opened from filing currently land in filing's own `CasesController` rather than
this service. The long-term intent is that filing's `CaseManagementPort` adapter
points to this service's `POST /api/v1/cases` endpoint. **This wiring is not
implemented yet** — it is a tracked follow-up integration decision. This service
defines an `InboundFilingCasePort` in its `api` layer to explicitly document the
contract this service intends to satisfy when that wiring is made.

---

## 9. Common command snippets

```bash
mvn test
mvn test -Dtest=ArchitectureTest
mvn test -Dtest=CaseTest
mvn test -Dtest=CreateCaseUseCaseTest
mvn spring-boot:run
```

---

## 10. When asked to add a new use case

1. Add `application/usecase/<area>/<Verb>CaseUseCase.java`.
2. Drain events: `aggregate.pullEvents().forEach(eventPublisher::publish)`.
3. Add request/response DTOs.
4. Wire into controller.
5. Add unit test covering happy + rejection + authorization path.
6. Add Bruno request.
7. Run `mvn test`.
