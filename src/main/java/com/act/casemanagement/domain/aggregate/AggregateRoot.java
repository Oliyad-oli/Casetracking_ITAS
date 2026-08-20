package com.act.casemanagement.domain.aggregate;

import com.act.casemanagement.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Base class for all aggregate roots in the Case Management service.
 * <p>
 * Identity: subclasses expose their own UUID id via {@link #getId()}.
 * Equality and hashCode are identity-based (id + concrete class) — final so
 * Mockito-inline cannot mock subclasses directly (use real instances in tests).
 * <p>
 * Domain events: aggregates record events via {@link #registerEvent}. Use cases
 * drain them via {@link #pullEvents()} after {@code save()} and dispatch them
 * via the event publisher. Events are NEVER published from inside an aggregate.
 */
public abstract class AggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /** The aggregate's stable identity. Must never return null after construction. */
    public abstract UUID getId();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }

    /**
     * Returns all pending domain events and clears the internal list.
     * Must be called by the use case on the <em>saved</em> aggregate,
     * not the in-memory one — otherwise the mock-returned object won't have events.
     */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> events = Collections.unmodifiableList(new ArrayList<>(domainEvents));
        domainEvents.clear();
        return events;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggregateRoot other)) return false;
        if (!getClass().equals(other.getClass())) return false;
        UUID myId = getId();
        UUID otherId = other.getId();
        return myId != null && myId.equals(otherId);
    }

    @Override
    public final int hashCode() {
        UUID id = getId();
        return id == null ? 0 : id.hashCode();
    }
}
