package com.chronos.core.api.event.discovery

import com.chronos.core.api.event.base.BaseEvent
import com.chronos.core.common.SourceType
import com.chronos.core.domain.model.GeoLocation
import com.chronos.core.util.id.IdentityGenerator
import java.time.Instant
import java.util.UUID

/**
* Triggered when a crawler discovers a user profile, channel, or entity.
* Corresponds to topic: chronos.profiles.v1
*/
data class ProfileDiscoveredEvent(

    /**
     * Unique ID inside the source platform (e.g., Instagram numeric ID).
     * Mandatory for deduplication.
     */
    val externalId: String,

    /**
     * Unique login or handle. Nullable if hidden/unknown.
     */
    val username: String? = null,

    /**
     * Display name (Nickname, Full Name).
     */
    val displayName: String? = null,

    /**
     * Direct URL to the profile.
     */
    val url: String? = null,

    /**
     * Link to the raw file in MinIO (The "Insurance Policy").
     * Required for "Raw Traceability" pattern.
     */
    val rawDataReference: String,

    /**
     * Structured Geo-data if available (moved to Core per Arch Refinements v2.0).
     */
    val location: GeoLocation? = null,

    // --- Flexible Payload (JSONB) ---

    /**
     * Source-specific fields (e.g., isPremium, reputation, pgpKey).
     * Stored as JSONB in TimescaleDB.
     */
    val details: Map<String, Any> = emptyMap(),

    // --- BaseEvent Overrides ---
    override val eventId: UUID,
    override val traceId: UUID,
    override val source: SourceType,
    override val observedAt: Instant = Instant.now()
) : BaseEvent(eventId, traceId, null, observedAt, observedAt, source) {
    companion object {
        fun create(
            source: SourceType,
            externalId: String,
            rawDataRef: String,
            traceId: UUID,
            username: String? = null,
            displayName: String? = null,
            url: String? = null,
            location: GeoLocation? = null,
            details: Map<String, Any> = emptyMap()
        ): ProfileDiscoveredEvent {

            val deterministicId = IdentityGenerator.generateEventId(source, externalId)

            return ProfileDiscoveredEvent(
                externalId = externalId,
                username = username,
                displayName = displayName,
                url = url,
                rawDataReference = rawDataRef,
                location = location,
                details = details,
                eventId = deterministicId,
                traceId = traceId,
                source = source
            )
        }
    }
}