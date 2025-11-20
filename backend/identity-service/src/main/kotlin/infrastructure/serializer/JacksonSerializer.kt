package com.chronos.identity.infrastructure.serializer

import com.chronos.core.common.abstraction.serializer.ChronosSerializer
import com.chronos.core.common.exception.CoreErrorCode
import com.chronos.core.common.exception.FatalException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class JacksonSerializer(private val objectMapper: ObjectMapper) : ChronosSerializer {

    override fun <T> serialize(obj: T): String {
        return try {
            objectMapper.writeValueAsString(obj)
        } catch (e: Exception) {
            throw FatalException(CoreErrorCode.SERIALIZATION_ERROR, "Failed to serialize object", cause = e)
        }
    }

    override fun <T> deserialize(content: String, clazz: Class<T>): T {
        return try {
            objectMapper.readValue(content, clazz)
        } catch (e: Exception) {
            throw FatalException(CoreErrorCode.SERIALIZATION_ERROR, "Failed to deserialize JSON", cause = e)
        }
    }
}