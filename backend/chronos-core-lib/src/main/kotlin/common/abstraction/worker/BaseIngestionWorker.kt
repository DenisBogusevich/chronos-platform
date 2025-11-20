package com.chronos.core.common.abstraction.worker

import com.chronos.core.api.command.IngestionCommand
import com.chronos.core.common.abstraction.AbstractChronosComponent
import java.time.Duration
import java.time.Instant

abstract class BaseIngestionWorker<T : IngestionCommand, R> : AbstractChronosComponent() {

    fun execute(command: T): R {
        return withTraceContextReturn(command.traceId) {
            val start = Instant.now()
            val workerName = this::class.simpleName

            logger.info("[WORKER_START] Component={} | Target={}", workerName, command.targetIdentifier)

            try {
                val result = doWork(command)

                val duration = Duration.between(start, Instant.now()).toMillis()
                logger.info("[WORKER_DONE] Component={} | Time={}ms | ResultSize={}",
                    workerName, duration, extractResultSize(result))

                return@withTraceContextReturn result

            } catch (e: Exception) {
                logger.error("[WORKER_FAIL] Component={} | Reason={}", workerName, e.message, e)
                throw e
            }
        }
    }

    protected abstract fun doWork(command: T): R

    protected open fun extractResultSize(result: R): Int = 1
}