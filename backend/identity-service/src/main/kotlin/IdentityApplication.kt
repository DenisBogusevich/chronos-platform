package com.chronos.identity

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import java.net.InetAddress
import java.util.TimeZone

@SpringBootApplication(
    scanBasePackages = [
        "com.chronos.identity",
        "com.chronos.core"
    ]
)
class IdentityApplication(private val env: Environment) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * CRITICAL: Enforce UTC TimeZone at application startup.
     * This ensures that all Instant.now() calls and DB interactions use UTC,
     * preventing timeline shifts in forensic data.
     */
    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        logger.info("Application timezone forced to UTC: {}", TimeZone.getDefault().id)
        logApplicationStartup()
    }

    private fun logApplicationStartup() {
        val protocol = if (env.getProperty("server.ssl.key-store") != null) "https" else "http"
        val port = env.getProperty("server.port")
        val contextPath = env.getProperty("server.servlet.context-path") ?: "/"
        val hostAddress = runCatching { InetAddress.getLocalHost().hostAddress }.getOrDefault("localhost")

        logger.info(
            """
            |
            |-------------------------------------------------------------------------
            |   Application '${env.getProperty("spring.application.name")}' is running!
            |   Access URLs:
            |   Local:      $protocol://localhost:$port$contextPath
            |   External:   $protocol://$hostAddress:$port$contextPath
            |   Profile(s): ${env.activeProfiles.joinToString(",")}
            |-------------------------------------------------------------------------
            """.trimMargin()
        )
    }
}

fun main(args: Array<String>) {
    runApplication<IdentityApplication>(*args)
}