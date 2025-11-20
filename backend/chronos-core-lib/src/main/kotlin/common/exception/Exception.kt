package com.chronos.core.common.exception

/**
 * Base interface for all system error codes.
 * Allows modules to define their own enums implementing this interface.
 */
interface ErrorCode {
    val code: String
    val description: String
}

/**
 * Standard Core Error Codes.
 */
enum class CoreErrorCode(override val code: String, override val description: String) : ErrorCode {
    INTERNAL_ERROR("CHRONOS-000", "Unexpected internal system error"),
    VALIDATION_ERROR("CHRONOS-001", "Input data failed validation"),
    SERIALIZATION_ERROR("CHRONOS-002", "Failed to serialize/deserialize data"),
    CONNECTIVITY_ERROR("CHRONOS-003", "External service or database is unreachable"),
    RESOURCE_NOT_FOUND("CHRONOS-004", "Requested resource was not found"),
    ACCESS_DENIED("CHRONOS-005", "Operation not permitted")
}

/**
 * The root of the Chronos exception hierarchy.
 * * @param errorCode Unique error code for cataloging/monitoring.
 * @param message Human-readable description.
 * @param context Key-value map for debugging (e.g., {"userId": "123", "docId": "abc"}).
 * @param isRetryable If true, the infrastructure should attempt to redeliver the message.
 */
abstract class ChronosException(
    val errorCode: ErrorCode,
    message: String,
    val context: Map<String, String> = emptyMap(),
    val isRetryable: Boolean = false,
    cause: Throwable? = null
) : RuntimeException("[${errorCode.code}] $message", cause)

/**
 * Represents a transient failure (Network blip, DB lock, Rate limit).
 * Strategy: RETRY with exponential backoff.
 */
class RetryableException(
    errorCode: ErrorCode,
    message: String,
    context: Map<String, String> = emptyMap(),
    cause: Throwable? = null
) : ChronosException(errorCode, message, context, isRetryable = true, cause = cause)

/**
 * Represents a permanent failure (Invalid JSON, Business rule violation).
 * Strategy: NO RETRY -> Send to DEAD LETTER QUEUE (DLQ).
 */
class FatalException(
    errorCode: ErrorCode,
    message: String,
    context: Map<String, String> = emptyMap(),
    cause: Throwable? = null
) : ChronosException(errorCode, message, context, isRetryable = false, cause = cause)