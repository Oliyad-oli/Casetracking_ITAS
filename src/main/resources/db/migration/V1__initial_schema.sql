-- ============================================================
-- RCMIS Case Management Service — Initial Schema
-- V1__initial_schema.sql
-- NEVER EDIT THIS FILE. Add V2__*, V3__* etc. for changes.
-- ============================================================

-- Audit log: append-only by convention; no UPDATE or DELETE granted
-- at the application layer (enforced via AuditLogPersistenceAdapter
-- which exposes only save(), never update/delete).
CREATE TABLE IF NOT EXISTS audit_logs (
    id              UUID        PRIMARY KEY,
    occurred_at     TIMESTAMPTZ NOT NULL,
    actor_id        UUID        NOT NULL,
    actor_name      VARCHAR(200),
    actor_role      VARCHAR(60) NOT NULL,
    action          VARCHAR(120) NOT NULL,
    case_id         UUID,
    case_number     VARCHAR(60),
    details         TEXT,
    previous_value  TEXT,
    new_value       TEXT,
    ip_address      VARCHAR(50),
    correlation_id  VARCHAR(100)
);
CREATE INDEX idx_audit_case_id ON audit_logs(case_id);
CREATE INDEX idx_audit_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_occurred_at ON audit_logs(occurred_at DESC);

-- Cases
CREATE TABLE IF NOT EXISTS cases (
    id                      UUID        PRIMARY KEY,
    version                 BIGINT      NOT NULL DEFAULT 0,
    case_number             VARCHAR(60) NOT NULL UNIQUE,
    title                   VARCHAR(500) NOT NULL,
    category                VARCHAR(60) NOT NULL,
    status                  VARCHAR(80) NOT NULL,
    priority                VARCHAR(20) NOT NULL,
    risk_level              VARCHAR(20) NOT NULL,
    tin                     VARCHAR(30) NOT NULL,
    taxpayer_name           VARCHAR(300) NOT NULL,
    entity_type             VARCHAR(50),

    created_by_id           UUID        NOT NULL,
    created_by_name         VARCHAR(200),
    created_at              TIMESTAMPTZ NOT NULL,
    assigning_officer_id    UUID,
    assigning_officer_name  VARCHAR(200),
    assigned_officer_id     UUID,
    assigned_officer_name   VARCHAR(200),
    assigned_unit_id        VARCHAR(60),
    assigned_unit_name      VARCHAR(200),
    assigned_date           TIMESTAMPTZ,
    assignment_reason       TEXT,
    is_self_assigned        BOOLEAN     NOT NULL DEFAULT FALSE,

    deadline                TIMESTAMPTZ,
    target_days             INT         NOT NULL DEFAULT 45,
    last_activity_at        TIMESTAMPTZ NOT NULL,
    reasons                 TEXT,
    instructions            TEXT,
    remarks                 TEXT,

    estimated_amount_etb    NUMERIC(20,2) NOT NULL DEFAULT 0,
    assessed_amount_etb     NUMERIC(20,2) NOT NULL DEFAULT 0,
    collected_amount_etb    NUMERIC(20,2) NOT NULL DEFAULT 0,
    written_off_amount_etb  NUMERIC(20,2) NOT NULL DEFAULT 0,
    refund_amount_etb       NUMERIC(20,2) NOT NULL DEFAULT 0,

    type_specific_data      JSONB,
    current_approval_level  VARCHAR(40),

    closure_reason          VARCHAR(60),
    closure_outcome         VARCHAR(60),
    closure_summary         TEXT,
    closure_amount_assessed NUMERIC(20,2),
    closure_amount_collected NUMERIC(20,2),
    closure_amount_written_off NUMERIC(20,2),
    closure_refund_approved NUMERIC(20,2),
    closed_by_id            UUID,
    closed_by_name          VARCHAR(200),
    closed_at               TIMESTAMPTZ,
    closure_doc_id          VARCHAR(100),

    is_draft                BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_cases_tin ON cases(tin);
CREATE INDEX idx_cases_status ON cases(status);
CREATE INDEX idx_cases_assigned_officer ON cases(assigned_officer_id);
CREATE INDEX idx_cases_assigning_officer ON cases(assigning_officer_id);
CREATE INDEX idx_cases_category ON cases(category);

-- Case Notes
CREATE TABLE IF NOT EXISTS case_notes (
    id               UUID        PRIMARY KEY,
    version          BIGINT      NOT NULL DEFAULT 0,
    case_id          UUID        NOT NULL REFERENCES cases(id),
    content          TEXT        NOT NULL,
    original_content TEXT        NOT NULL,
    event_date       DATE        NOT NULL,
    recording_date   TIMESTAMPTZ NOT NULL,
    author_id        UUID        NOT NULL,
    author_name      VARCHAR(200) NOT NULL,
    author_role      VARCHAR(60) NOT NULL,
    is_obsolete      BOOLEAN     NOT NULL DEFAULT FALSE,
    obsolete_reason  TEXT,
    is_edited        BOOLEAN     NOT NULL DEFAULT FALSE,
    edit_history     JSONB,
    attachment_name  VARCHAR(300)
);
CREATE INDEX idx_notes_case_id ON case_notes(case_id);

-- Case Documents
CREATE TABLE IF NOT EXISTS case_documents (
    id              UUID        PRIMARY KEY,
    version         BIGINT      NOT NULL DEFAULT 0,
    case_id         UUID        NOT NULL REFERENCES cases(id),
    name            VARCHAR(500) NOT NULL,
    document_type   VARCHAR(60) NOT NULL,
    file_type       VARCHAR(20),
    size_in_mb      NUMERIC(10,3),
    uploaded_by_id  UUID        NOT NULL,
    uploaded_by_name VARCHAR(200),
    uploaded_at     TIMESTAMPTZ NOT NULL,
    classification  VARCHAR(30) NOT NULL,
    doc_version     VARCHAR(20),
    dms_reference   VARCHAR(200),
    preview_url     VARCHAR(500),
    download_url    VARCHAR(500)
);
CREATE INDEX idx_docs_case_id ON case_documents(case_id);

-- Correspondence
CREATE TABLE IF NOT EXISTS correspondences (
    id               UUID        PRIMARY KEY,
    case_id          UUID        NOT NULL REFERENCES cases(id),
    reference_no     VARCHAR(80) NOT NULL,
    recipient_name   VARCHAR(300),
    recipient_address TEXT,
    template_id      VARCHAR(80),
    template_title   VARCHAR(200),
    channel          VARCHAR(20) NOT NULL,
    direction        VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    sent_at          TIMESTAMPTZ NOT NULL,
    delivered_at     TIMESTAMPTZ,
    subject          VARCHAR(500),
    content          TEXT,
    attachments      JSONB,
    created_by_id    UUID        NOT NULL
);
CREATE INDEX idx_corr_case_id ON correspondences(case_id);

-- Task Reminders
CREATE TABLE IF NOT EXISTS task_reminders (
    id                   UUID        PRIMARY KEY,
    version              BIGINT      NOT NULL DEFAULT 0,
    case_id              UUID        NOT NULL REFERENCES cases(id),
    case_number          VARCHAR(60),
    title                VARCHAR(300) NOT NULL,
    description          TEXT,
    due_date             DATE        NOT NULL,
    due_time             VARCHAR(10),
    priority             VARCHAR(20) NOT NULL,
    assigned_to_user_id  UUID        NOT NULL,
    assigned_to_name     VARCHAR(200),
    recipient_type       VARCHAR(30),
    status               VARCHAR(20) NOT NULL,
    reminder_frequency   VARCHAR(20) NOT NULL DEFAULT 'ONCE',
    linked_activity_type VARCHAR(60),
    cancellation_reason  TEXT,
    completed_at         TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_tasks_case_id ON task_reminders(case_id);
CREATE INDEX idx_tasks_assigned_user ON task_reminders(assigned_to_user_id);
CREATE INDEX idx_tasks_status ON task_reminders(status);

-- Approval Records
CREATE TABLE IF NOT EXISTS approval_records (
    id              UUID        PRIMARY KEY,
    case_id         UUID        NOT NULL REFERENCES cases(id),
    level           VARCHAR(40) NOT NULL,
    approver_id     UUID        NOT NULL,
    approver_name   VARCHAR(200),
    approver_role   VARCHAR(60) NOT NULL,
    action          VARCHAR(30) NOT NULL,
    comments        TEXT,
    occurred_at     TIMESTAMPTZ NOT NULL,
    next_level      VARCHAR(40)
);
CREATE INDEX idx_approvals_case_id ON approval_records(case_id);

-- Case Associations
CREATE TABLE IF NOT EXISTS case_associations (
    id                   UUID        PRIMARY KEY,
    source_case_id       UUID        NOT NULL REFERENCES cases(id),
    target_case_id       UUID        NOT NULL,
    target_case_number   VARCHAR(60),
    target_case_title    VARCHAR(500),
    relationship_type    VARCHAR(60) NOT NULL,
    notes                TEXT,
    created_by_id        UUID        NOT NULL,
    created_by_name      VARCHAR(200),
    created_at           TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_assoc_source ON case_associations(source_case_id);
CREATE INDEX idx_assoc_target ON case_associations(target_case_id);

-- Case Type Configuration
CREATE TABLE IF NOT EXISTS case_type_configs (
    id                  UUID        PRIMARY KEY,
    version             BIGINT      NOT NULL DEFAULT 0,
    code                VARCHAR(60) NOT NULL UNIQUE,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    is_active           BOOLEAN     NOT NULL DEFAULT TRUE,
    effective_date      DATE        NOT NULL,
    config_version      VARCHAR(20),
    default_target_days INT         NOT NULL DEFAULT 45,
    fields_definition   JSONB,
    allowed_statuses    JSONB,
    approval_levels     JSONB,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);

-- Workflow Stage Configuration
CREATE TABLE IF NOT EXISTS workflow_stage_configs (
    id               UUID        PRIMARY KEY,
    version          BIGINT      NOT NULL DEFAULT 0,
    name             VARCHAR(200) NOT NULL,
    category         VARCHAR(60),
    description      TEXT,
    status           VARCHAR(80) NOT NULL,
    allowed_roles    JSONB       NOT NULL,
    allowed_actions  JSONB,
    sla_hours        INT         NOT NULL DEFAULT 48,
    next_status      VARCHAR(80),
    is_active        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL
);

-- Approval Matrix Configuration
CREATE TABLE IF NOT EXISTS approval_matrix_configs (
    id                 UUID        PRIMARY KEY,
    version            BIGINT      NOT NULL DEFAULT 0,
    case_type          VARCHAR(60) NOT NULL,
    min_value_etb      NUMERIC(20,2) NOT NULL DEFAULT 0,
    max_value_etb      NUMERIC(20,2),
    risk_level         VARCHAR(20) NOT NULL,
    required_approvals JSONB       NOT NULL,
    is_active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL
);

-- Assignment Weights Configuration
CREATE TABLE IF NOT EXISTS assignment_weights_configs (
    id                    UUID        PRIMARY KEY,
    version               BIGINT      NOT NULL DEFAULT 0,
    capacity_weight       NUMERIC(5,4) NOT NULL DEFAULT 0.35,
    specialization_weight NUMERIC(5,4) NOT NULL DEFAULT 0.35,
    region_weight         NUMERIC(5,4) NOT NULL DEFAULT 0.15,
    caseload_mix_weight   NUMERIC(5,4) NOT NULL DEFAULT 0.15,
    is_active             BOOLEAN     NOT NULL DEFAULT TRUE,
    effective_from        TIMESTAMPTZ NOT NULL,
    created_by_id         UUID,
    created_at            TIMESTAMPTZ NOT NULL
);

-- Outbox (at-least-once delivery for cross-service side effects)
CREATE TABLE IF NOT EXISTS outbox_entries (
    id              UUID        PRIMARY KEY,
    event_type      VARCHAR(120) NOT NULL,
    payload         JSONB       NOT NULL,
    aggregate_id    UUID        NOT NULL,
    aggregate_type  VARCHAR(80) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts        INT         NOT NULL DEFAULT 0,
    max_attempts    INT         NOT NULL DEFAULT 5,
    last_error      TEXT,
    created_at      TIMESTAMPTZ NOT NULL,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    sent_at         TIMESTAMPTZ
);
CREATE INDEX idx_outbox_status_next ON outbox_entries(status, next_attempt_at)
    WHERE status IN ('PENDING', 'RETRY');

-- Idempotency store (POST replay protection)
CREATE TABLE IF NOT EXISTS idempotency_store (
    idempotency_key VARCHAR(100) PRIMARY KEY,
    response_status INT         NOT NULL,
    response_body   TEXT,
    created_at      TIMESTAMPTZ NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_idempotency_expires ON idempotency_store(expires_at);
