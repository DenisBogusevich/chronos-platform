package com.chronos.core.common.abstraction.messaging

import com.chronos.core.api.event.base.DomainEvent

/**
 * Component responsible for dispatching domain events to the Message Broker (e.g., Kafka).
 */
interface EventPublisher {

    /**
     * Publishes a single event to the appropriate topic.
     * The topic name is usually derived from the event type.
     * * @param event The domain event to publish.
     */
    fun publish(event: DomainEvent)

    /**
     * Batch publication for high-throughput scenarios.
     */
    fun publishBatch(events: List<DomainEvent>)
}