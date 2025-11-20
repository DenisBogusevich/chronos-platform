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

    /**
     * Flexible configuration for the worker.
     * Reserved keys:
     * - "proxy_country": ISO country code for VPN/Proxy exit node.
     * - "user_agent_mode": "MOBILE" | "DESKTOP".
     * - "max_depth": Recursion depth for scraping.
     */
    val metadata: Map<String, String> = emptyMap(),

    val createdAt: Instant = Instant.now()
)

object MetadataKeys {
    const val PROXY_COUNTRY = "proxy_country"
    const val USER_AGENT_MODE = "user_agent_mode"
    const val MAX_DEPTH = "max_depth"
    const val TOR_EXIT_NODE = "tor_exit_node"
}

enum class UserAgentMode { MOBILE, DESKTOP, BOT }

// --- Extension Properties ---

/**
 * ISO Country Code for VPN/Proxy exit node (e.g., "US", "DE").
 */
var IngestionCommand.proxyCountry: String?
    get() = this.metadata[MetadataKeys.PROXY_COUNTRY]
    set(value) {
        // Note: Data classes are immutable by default, this set logic implies
        // you are building a MutableMap before creating the command,
        // or we provide a builder helper.
        // For an immutable command, we just use the getter.
    }

/**
 * Helper to retrieve specific configs with defaults.
 */
fun IngestionCommand.getProxyCountryOrDefault(default: String = "US"): String {
    return this.metadata[MetadataKeys.PROXY_COUNTRY] ?: default
}

fun IngestionCommand.getUserAgentMode(): UserAgentMode {
    val value = this.metadata[MetadataKeys.USER_AGENT_MODE]
    return try {
        if (value != null) UserAgentMode.valueOf(value) else UserAgentMode.DESKTOP
    } catch (e: IllegalArgumentException) {
        UserAgentMode.DESKTOP // Fallback
    }
}

fun IngestionCommand.getMaxDepth(): Int {
    return this.metadata[MetadataKeys.MAX_DEPTH]?.toIntOrNull() ?: 1
}

/**
 * Builder helper to construct metadata safely.
 */
class IngestionMetadataBuilder {
    private val map = mutableMapOf<String, String>()

    fun withProxyCountry(isoCode: String) = apply { map[MetadataKeys.PROXY_COUNTRY] = isoCode }
    fun withMobileUserAgent() = apply { map[MetadataKeys.USER_AGENT_MODE] = UserAgentMode.MOBILE.name }
    fun withMaxDepth(depth: Int) = apply { map[MetadataKeys.MAX_DEPTH] = depth.toString() }

    fun build(): Map<String, String> = map.toMap()
}