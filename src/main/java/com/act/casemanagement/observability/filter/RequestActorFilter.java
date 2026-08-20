package com.act.casemanagement.observability.filter;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.domain.valueobject.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Section 5.1 fix — reads the gateway-forwarded identity headers once per
 * request and populates RequestActorContext.
 *
 * Headers set by the API gateway (never by the client directly):
 *   X-Authenticated-Actor-Id  → the authenticated user's UUID
 *   X-Authenticated-Role       → the authenticated user's role
 *   X-Authenticated-Unit-Id    → the authenticated user's organisational unit UUID
 *
 * Controllers and use cases MUST read the actor from RequestActorContext.current()
 * — never from a request body field.
 *
 * The ThreadLocal is always cleared in the finally block to prevent cross-request
 * leakage in thread-pool environments.
 */
@Slf4j
@Component
@Order(1)
public class RequestActorFilter extends OncePerRequestFilter {

    static final String HEADER_ACTOR_ID = "X-Authenticated-Actor-Id";
    static final String HEADER_ROLE     = "X-Authenticated-Role";
    static final String HEADER_UNIT_ID  = "X-Authenticated-Unit-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {
            String rawActorId = request.getHeader(HEADER_ACTOR_ID);
            String rawRole    = request.getHeader(HEADER_ROLE);
            String rawUnitId  = request.getHeader(HEADER_UNIT_ID);

            UUID actorId = parseUuid(rawActorId);
            UserRole role = parseRole(rawRole);
            String unitId = (rawUnitId != null && !rawUnitId.isBlank()) ? rawUnitId.trim() : null;

            RequestActorContext.set(actorId, role, unitId);
            log.debug("RequestActorFilter: actorId={} role={} unitId={}", actorId, role, unitId);

            chain.doFilter(request, response);

        } finally {
            RequestActorContext.clear();
        }
    }

    private UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            // No gateway header — use a sentinel value so unauthenticated requests
            // reach the controller and get rejected with a proper 401/403 from the
            // gateway before they reach us. If they do reach us without a header
            // it means the gateway misconfigured; we log a warning and use a nil UUID.
            log.warn("Missing {} header — request may not have passed through the gateway", HEADER_ACTOR_ID);
            return new UUID(0L, 0L);
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid {} header value: {}", HEADER_ACTOR_ID, raw);
            return new UUID(0L, 0L);
        }
    }

    private UserRole parseRole(String raw) {
        if (raw == null || raw.isBlank()) {
            log.warn("Missing {} header", HEADER_ROLE);
            return UserRole.TAX_OFFICER; // lowest privilege default
        }
        try {
            return UserRole.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role in {} header: {}", HEADER_ROLE, raw);
            return UserRole.TAX_OFFICER;
        }
    }
}
