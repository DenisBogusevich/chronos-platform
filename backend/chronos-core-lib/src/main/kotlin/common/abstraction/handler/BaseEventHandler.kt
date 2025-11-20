package com.chronos.core.common.abstraction.handler

import com.chronos.core.api.event.base.DomainEvent
import com.chronos.core.common.abstraction.AbstractChronosComponent

abstract class BaseEventHandler<E : DomainEvent>(
    private val eventClass: Class<E>
) : AbstractChronosComponent() {

    fun onMessage(payload: String) {
        try {
            val event: E = parseEvent(payload)

            withTraceContext(event.traceId) {
                logger.info("[EVENT_RECEIVED] Type={} | ID={} | Source={}",
                    event::class.simpleName, event.eventId, event.source)

                handleEvent(event)

                logger.info("[EVENT_PROCESSED] ID={}", event.eventId)
            }
        } catch (e: Exception) {
            logger.error("[EVENT_ERROR] Payload fragment={} | Error={}", payload.take(100), e.message)
        }
    }

    protected abstract fun handleEvent(event: E)

    private fun parseEvent(json: String): E {
        // return objectMapper.readValue(json, eventClass)
        TODO("Inject ObjectMapper here")
    }
}