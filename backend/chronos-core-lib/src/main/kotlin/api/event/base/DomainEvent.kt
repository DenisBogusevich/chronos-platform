package com.chronos.core.api.event.base

import com.chronos.core.common.SourceType
import com.chronos.core.common.TLPLevel
import java.time.Instant
import java.util.UUID

interface DomainEvent {
    val eventId: UUID
    val traceId: UUID
    val investigationId: UUID?
    val occurredAt: Instant
    val observedAt: Instant
    val source: SourceType
    val tlp: TLPLevel

    fun toAuditLog(): String
}


abstract class BaseEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val traceId: UUID,
    override val investigationId: UUID? = null,
    override val occurredAt: Instant = Instant.now(),
    override val observedAt: Instant = Instant.now(),
    override val source: SourceType,
    override val tlp: TLPLevel = TLPLevel.AMBER
) : DomainEvent {

    override fun toAuditLog(): String {
        return "[EVENT] TYPE=${this::class.simpleName} | ID=$eventId | TRACE=$traceId | SRC=$source"
    }
}