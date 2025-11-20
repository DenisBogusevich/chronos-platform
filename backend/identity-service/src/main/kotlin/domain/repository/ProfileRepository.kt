package com.chronos.identity.domain.repository

import com.chronos.core.api.event.discovery.ProfileDiscoveredEvent
import com.chronos.core.common.abstraction.serializer.ChronosSerializer
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProfileRepository(
    private val ctx: DSLContext,
    private val serializer: ChronosSerializer // Используем наш сериализатор для JSONB
) {

    @Transactional
    fun upsertProfile(event: ProfileDiscoveredEvent) {

        val detailsJson = JSONB.valueOf(serializer.serialize(event.details))

        val locationField = if (event.location != null && event.location!!.hasCoordinates()) {
            DSL.field(
                "ST_SetSRID(ST_MakePoint({0}, {1}), 4326)::geography",
                Any::class.java,
                event.location!!.longitude,
                event.location!!.latitude
            )
        } else {
            DSL.val(null)
        }

        ctx.insertInto(PROFILES)
            .set(PROFILES.EVENT_ID, event.eventId)
            .set(PROFILES.SOURCE, event.source.name)
            .set(PROFILES.EXTERNAL_ID, event.externalId)
            .set(PROFILES.USERNAME, event.username)
            .set(PROFILES.DISPLAY_NAME, event.displayName)
            .set(PROFILES.URL, event.url)
            .set(PROFILES.RAW_DATA_REFERENCE, event.rawDataReference)
            .set(PROFILES.TRACE_ID, event.traceId)
            .set(PROFILES.OBSERVED_AT, event.observedAt.atOffset(java.time.ZoneOffset.UTC)) // TimescaleDB любит OffsetDateTime
            .set(PROFILES.DETAILS, detailsJson)
            .set(PROFILES.LOCATION, locationField) // Вставляем наше вычисляемое поле
            .onConflict(PROFILES.SOURCE, PROFILES.EXTERNAL_ID) // Уникальный ключ
            .doUpdate()
            .set(PROFILES.USERNAME, DSL.coalesce(DSL.excluded(PROFILES.USERNAME), PROFILES.USERNAME))
            .set(PROFILES.DISPLAY_NAME, DSL.coalesce(DSL.excluded(PROFILES.DISPLAY_NAME), PROFILES.DISPLAY_NAME))
            .set(PROFILES.URL, DSL.coalesce(DSL.excluded(PROFILES.URL), PROFILES.URL))
            .set(PROFILES.LOCATION, DSL.coalesce(DSL.excluded(PROFILES.LOCATION), PROFILES.LOCATION))
            // Мержим JSONB: старые данные || новые данные
            .set(
                PROFILES.DETAILS,
                DSL.field("{0} || {1}", JSONB::class.java, PROFILES.DETAILS, DSL.excluded(PROFILES.DETAILS))
            )
            .set(PROFILES.TRACE_ID, DSL.excluded(PROFILES.TRACE_ID))
            .set(PROFILES.OBSERVED_AT, DSL.greatest(PROFILES.OBSERVED_AT, DSL.excluded(PROFILES.OBSERVED_AT)))
            .set(PROFILES.UPDATED_AT, DSL.currentOffsetDateTime())
            .execute()
    }
}