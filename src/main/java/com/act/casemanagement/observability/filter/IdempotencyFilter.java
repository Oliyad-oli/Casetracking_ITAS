package com.act.casemanagement.observability.filter;

import com.act.casemanagement.application.port.IdempotencyStorePort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * POST replay-protection — mirrors the filing-service IdempotencyFilter pattern.
 * If the Idempotency-Key header is present on a POST:
 *   - Hit: return cached response immediately.
 *   - Miss: execute chain, cache response (capped at 64 KiB) for 24 h.
 * Only caches JSON responses (application/json).
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String   HEADER_KEY        = "Idempotency-Key";
    private static final int      MAX_BODY_BYTES     = 64 * 1024;
    private static final long     TTL_SECONDS        = 86_400L;
    private static final Set<String> ELIGIBLE_TYPES  = Set.of(MediaType.APPLICATION_JSON_VALUE);

    private final IdempotencyStorePort idempotencyStore;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String key = request.getHeader(HEADER_KEY);
        if (key == null || key.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        // Cache hit — return stored response
        var cached = idempotencyStore.findByKey(key);
        if (cached.isPresent()) {
            log.debug("Idempotency cache hit for key={}", key);
            response.setStatus(cached.get().status());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(cached.get().responseBody());
            return;
        }

        // Cache miss — execute and capture
        var wrapper = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, wrapper);

        String contentType = wrapper.getContentType();
        if (contentType != null && ELIGIBLE_TYPES.stream()
                .anyMatch(contentType::contains)) {
            byte[] body = wrapper.getContentAsByteArray();
            if (body.length <= MAX_BODY_BYTES) {
                String bodyStr = new String(body, StandardCharsets.UTF_8);
                idempotencyStore.store(key, wrapper.getStatus(), bodyStr, TTL_SECONDS);
                log.debug("Idempotency cached key={} status={}", key, wrapper.getStatus());
            }
        }
        wrapper.copyBodyToResponse();
    }
}
