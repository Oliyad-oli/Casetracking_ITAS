package com.act.casemanagement.observability.audit;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.AuditLogPort;
import com.act.casemanagement.domain.exception.ForbiddenOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Section 5.3 — wraps every @Auditable use-case method with START / SUCCESS / FAILURE
 * audit entries. Authorization rejections (ForbiddenOperationException) are always
 * captured as FAILURE entries with the specific reason — not just a generic exception log.
 *
 * This satisfies CTR0500 note-integrity and CTR0700 closure-restriction audit requirements:
 * the evidence trail is provable after the fact, not just enforced in the moment.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditInterceptor {

    private final AuditLogPort auditLogPort;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        String action       = auditable.action();
        String correlationId = MDC.get("correlationId");
        UUID   auditId      = UUID.randomUUID();

        // Safely resolve actor — may be absent in tests or async contexts
        UUID   actorId   = null;
        String actorRole = "UNKNOWN";
        try {
            RequestActorContext ctx = RequestActorContext.current();
            actorId   = ctx.getActorId();
            actorRole = ctx.getRole().name();
        } catch (IllegalStateException ignored) {}

        log.debug("AUDIT START action={} correlationId={}", action, correlationId);

        try {
            Object result = pjp.proceed();

            auditLogPort.save(new AuditLogPort.AuditEntry(
                    auditId, Instant.now(), actorId, null, actorRole,
                    action + "_SUCCESS", null, null,
                    "Method: " + pjp.getSignature().toShortString(),
                    null, null, null, correlationId));

            log.debug("AUDIT SUCCESS action={}", action);
            return result;

        } catch (ForbiddenOperationException foe) {
            // Section 5.3: authorization rejections get their own FAILURE entry with
            // the specific operation and resource, not just a generic message.
            auditLogPort.save(new AuditLogPort.AuditEntry(
                    UUID.randomUUID(), Instant.now(), actorId, null, actorRole,
                    action + "_FORBIDDEN", null, null,
                    "FORBIDDEN: actor " + foe.getActorId()
                            + " attempted [" + foe.getOperation()
                            + "] on resource " + foe.getResourceId(),
                    null, foe.getMessage(), null, correlationId));

            log.warn("AUDIT FORBIDDEN action={} actorId={} operation={} resource={}",
                    action, foe.getActorId(), foe.getOperation(), foe.getResourceId());
            throw foe;

        } catch (Throwable t) {
            auditLogPort.save(new AuditLogPort.AuditEntry(
                    UUID.randomUUID(), Instant.now(), actorId, null, actorRole,
                    action + "_FAILURE", null, null,
                    "FAILURE: " + t.getMessage(),
                    null, null, null, correlationId));

            log.error("AUDIT FAILURE action={} error={}", action, t.getMessage());
            throw t;
        }
    }
}
