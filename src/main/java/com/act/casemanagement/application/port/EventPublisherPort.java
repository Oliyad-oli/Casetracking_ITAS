package com.act.casemanagement.application.port;

import com.act.casemanagement.domain.event.DomainEvent;

/**
 * Spring ApplicationEventPublisher abstraction — wraps domain events so
 * the domain never imports Spring. Use cases drain events from the aggregate
 * after save() and dispatch via this port.
 */
public interface EventPublisherPort {
    void publish(DomainEvent event);
}
