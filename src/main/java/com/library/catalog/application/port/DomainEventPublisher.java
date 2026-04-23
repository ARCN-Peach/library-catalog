package com.library.catalog.application.port;

import com.library.catalog.domain.event.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {

    void publish(List<DomainEvent> events);
}
