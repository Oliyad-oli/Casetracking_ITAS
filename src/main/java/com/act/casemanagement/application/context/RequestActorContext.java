package com.act.casemanagement.application.context;

import com.act.casemanagement.domain.valueobject.UserRole;
import lombok.Getter;

import java.util.UUID;

/**
 * Holds the gateway-authenticated actor identity for the current request thread.
 * <p>
 * Populated by {@code RequestActorFilter} from gateway-only headers:
 * <ul>
 *   <li>{@code X-Authenticated-Actor-Id}  → actorId</li>
 *   <li>{@code X-Authenticated-Role}       → role</li>
 *   <li>{@code X-Authenticated-Unit-Id}    → unitId</li>
 * </ul>
 * <p>
 * Use cases read the actor from this context — NEVER from request DTO body fields.
 * The ThreadLocal is cleared by the filter's finally block to prevent leaking
 * across request boundaries in thread-pool environments.
 */
@Getter
public class RequestActorContext {

    private static final ThreadLocal<RequestActorContext> HOLDER = new ThreadLocal<>();

    private final UUID actorId;
    private final UserRole role;
    private final String unitId;

    private RequestActorContext(UUID actorId, UserRole role, String unitId) {
        this.actorId = actorId;
        this.role    = role;
        this.unitId  = unitId;
    }

    // ── static lifecycle ──────────────────────────────────────────────────────

    /** Called by RequestActorFilter at the start of every request. */
    public static void set(UUID actorId, UserRole role, String unitId) {
        HOLDER.set(new RequestActorContext(actorId, role, unitId));
    }

    /**
     * Returns the current request's actor context.
     * @throws IllegalStateException if called outside a request (no filter ran)
     */
    public static RequestActorContext current() {
        RequestActorContext ctx = HOLDER.get();
        if (ctx == null) {
            throw new IllegalStateException(
                "No RequestActorContext — was the request processed by RequestActorFilter?");
        }
        return ctx;
    }

    /** Called by RequestActorFilter in its finally block. */
    public static void clear() {
        HOLDER.remove();
    }
}
