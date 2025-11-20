package com.chronos.core.api.command

import com.chronos.core.common.IngestionStrategy
import com.chronos.core.common.SourceType
import java.time.Instant
import java.util.UUID

data class IngestionCommand(
    val commandId: UUID = UUID.randomUUID(),

    val traceId: UUID,

    val targetSource: SourceType,
    val targetIdentifier: String,

    val strategy: IngestionStrategy = IngestionStrategy.WEB_SCRAPING,

    val metadata: Map<String, String> = emptyMap(),

    val createdAt: Instant = Instant.now()
)