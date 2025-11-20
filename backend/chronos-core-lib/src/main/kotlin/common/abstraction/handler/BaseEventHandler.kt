package com.chronos.core.common.abstraction.handler

import com.chronos.core.api.event.base.DomainEvent
import com.chronos.core.common.abstraction.AbstractChronosComponent
import com.chronos.core.common.abstraction.serializer.ChronosSerializer
import com.chronos.core.common.exception.ChronosException
import com.chronos.core.common.exception.CoreErrorCode
import com.chronos.core.common.exception.FatalException
import org.slf4j.MDC

/**
 * Skeleton for all Event Consumers.
 */
abstract class BaseEventHandler<E : DomainEvent>(
    private val eventClass: Class<E>,
    private val serializer: ChronosSerializer
) : AbstractChronosComponent() {

    /**
     * Entry point for the message broker listener.
     */
    fun onMessage(payload: String) {
        var eventIdForLogs: String = "UNKNOWN"

        try {
            // 1. Deserialization (Failures here are FATAL)
            val event: E = try {
                serializer.deserialize(payload, eventClass)
            } catch (e: Exception) {
                throw FatalException(
                    errorCode = CoreErrorCode.SERIALIZATION_ERROR,
                    message = "Failed to parse event payload",
                    context = mapOf("payload_snippet" to payload.take(200)),
                    cause = e
                )
            }

            eventIdForLogs = event.eventId.toString()

            // 2. Processing Loop with Context
            withTraceContext(event.traceId) {
                MDC.put("event_id", eventIdForLogs)
                MDC.put("source", event.source.name)

                logger.info("[EVENT_RECEIVED] Type={}", eventClass.simpleName)

                handleEvent(event)

                logger.info("[EVENT_PROCESSED] Successfully processed")
            }

        } catch (e: ChronosException) {
            // 3. Handle Known System Exceptions
            handleChronosException(payload, eventIdForLogs, e)
        } catch (e: Throwable) {
            // 4. Handle Unexpected/Unknown Exceptions (Default to Retry or Fatal depending on policy)
            // Usually, unexpected RuntimeExceptions (NPE, IndexOutOfBounds) are bugs -> Fatal
            // But SQL Connection errors are also Runtime -> Retryable.
            // Let's wrap it safely.
            val wrapped = FatalException(
                errorCode = CoreErrorCode.INTERNAL_ERROR,
                message = "Unexpected handler error: ${e.message}",
                cause = e
            )
            handleChronosException(payload, eventIdForLogs, wrapped)
        } finally {
            MDC.remove("event_id")
            MDC.remove("source")
        }
    }

    protected abstract fun handleEvent(event: E)

    private fun handleChronosException(payload: String, eventId: String, e: ChronosException) {
        val logContext = e.context + mapOf("event_id" to eventId, "error_code" to e.errorCode.code)

        if (e.isRetryable) {
            logger.warn("[EVENT_RETRY] Transient error occurred. Re-throwing to infrastructure. Code={} | Msg={}",
                e.errorCode.code, e.message)
            // Re-throwing allows Kafka/RabbitMQ to trigger their backoff retry policy
            throw e
        } else {
            logger.error("[EVENT_FAILURE] Fatal error. Moving to DLQ. Code={} | Msg={} | Context={}",
                e.errorCode.code, e.message, logContext, e)

            // Execute DLQ Logic
            sendToDeadLetterQueue(payload, e)
        }
    }

    /**
     * Strategy for handling Poison Pills.
     * Default implementation logs, but in production, this should write to a 'dead-letter' topic or table.
     */
    protected open fun sendToDeadLetterQueue(payload: String, error: ChronosException) {
        // TODO: In the implementation layer, inject a 'DlqProducer' and send the payload there.
        // For now, we log strictly as ERROR to be picked up by log monitors.
        logger.error("[DLQ_DUMP] Payload: $payload")
    }
}