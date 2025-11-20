package com.chronos.core.common.abstraction

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.UUID

abstract class AbstractChronosComponent {

    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val TRACE_ID_KEY = "trace_id"
    }

    protected fun withTraceContext(traceId: UUID, block: () -> Unit) {
        MDC.put(TRACE_ID_KEY, traceId.toString())
        try {
            block()
        } finally {
            MDC.remove(TRACE_ID_KEY)
        }
    }

    protected fun <T> withTraceContextReturn(traceId: UUID, block: () -> T): T {
        MDC.put(TRACE_ID_KEY, traceId.toString())
        try {
            return block()
        } finally {
            MDC.remove(TRACE_ID_KEY)
        }
    }

}