package com.chronos.core.util.id

import com.chronos.core.common.SourceType
import java.nio.charset.StandardCharsets
import java.util.UUID

object IdentityGenerator {

    private val CHRONOS_NAMESPACE: UUID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")

    fun generateEventId(source: SourceType, externalId: String): UUID {
        val normalizedKey = normalize(source, externalId)
        return generateV5(normalizedKey)
    }

    fun generateCompositeId(source: SourceType, vararg parts: String): UUID {
        val compositeKey = parts.joinToString(":") { it.trim().lowercase() }
        val fullKey = "${source.name}:$compositeKey"
        return generateV5(fullKey)
    }

    private fun normalize(source: SourceType, id: String): String {
        return "${source.name}:${id.trim().lowercase()}"
    }

    private fun generateV5(key: String): UUID {
        val bytes = key.toByteArray(StandardCharsets.UTF_8)
        return UUID.nameUUIDFromBytes(bytes)
    }
}