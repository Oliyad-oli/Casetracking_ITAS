package com.act.casemanagement.config;

import com.act.casemanagement.application.port.EventPublisherPort;
import com.act.casemanagement.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Wraps Spring's ApplicationEventPublisher so the domain and application layers
 * never import Spring directly. Use cases depend only on EventPublisherPort.
 */
@Component
@RequiredArgsConstructor
public class SpringEventPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
