package com.chronos.core.common.abstraction.serializer

/**
 * Standard interface for object serialization to decouple
 * the core logic from specific JSON libraries (Jackson/Gson).
 */
interface ChronosSerializer {
    fun <T> serialize(obj: T): String
    fun <T> deserialize(content: String, clazz: Class<T>): T
}