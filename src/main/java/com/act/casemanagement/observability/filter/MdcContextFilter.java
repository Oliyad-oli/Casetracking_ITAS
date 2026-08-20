package com.act.casemanagement.observability.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Propagates correlation ID and actor ID into SLF4J MDC for structured logging.
 * Runs after RequestActorFilter (Order 2).
 */
@Slf4j
@Component
@Order(2)
public class MdcContextFilter extends OncePerRequestFilter {

    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_ACTOR_ID       = "actorId";
    private static final String HEADER_CORRELATION  = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        try {
            String correlationId = request.getHeader(HEADER_CORRELATION);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(MDC_CORRELATION_ID, correlationId);
            MDC.put(MDC_ACTOR_ID, request.getHeader(RequestActorFilter.HEADER_ACTOR_ID) != null
                    ? request.getHeader(RequestActorFilter.HEADER_ACTOR_ID) : "UNKNOWN");

            response.setHeader(HEADER_CORRELATION, correlationId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_ACTOR_ID);
        }
    }
}
