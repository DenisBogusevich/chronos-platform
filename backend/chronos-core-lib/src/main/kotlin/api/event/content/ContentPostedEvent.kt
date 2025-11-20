package com.chronos.core.api.event.content

import com.chronos.core.api.event.base.BaseEvent
import com.chronos.core.common.SourceType
import com.chronos.core.util.id.IdentityGenerator
import java.time.Instant
import java.util.UUID

data class ContentPostedEvent(
    val externalId: String,
    val authorExternalId: String,

    val text: String? = null,
    val language: String? = null,
    val createdAt: Instant? = null,
    val attachments: List<Attachment> = emptyList(),
    val metrics: Map<String, Long> = emptyMap(),

    override val eventId: UUID,
    override val traceId: UUID,
    override val source: SourceType
) : BaseEvent(eventId = eventId, traceId = traceId, source = source) {

    companion object {
        fun create(
            source: SourceType,
            externalId: String,
            authorExternalId: String,
            traceId: UUID,
            text: String? = null,
            createdAt: Instant? = null,
            language: String? = null,
            attachments: List<Attachment> = emptyList(),
            metrics: Map<String, Long> = emptyMap()
        ): ContentPostedEvent {

            val deterministicId = IdentityGenerator.generateEventId(source, externalId)

            return ContentPostedEvent(
                externalId = externalId,
                authorExternalId = authorExternalId,
                text = text,
                language = language,
                createdAt = createdAt,
                attachments = attachments,
                metrics = metrics,
                eventId = deterministicId,
                traceId = traceId,
                source = source
            )
        }
    }
}

data class Attachment(
    val type: AttachmentType,
    val url: String,
    val fileName: String? = null
)

enum class AttachmentType {
    IMAGE, VIDEO, DOCUMENT, AUDIO, ARCHIVE, OTHER
}