package com.chronos.core.domain.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable


@JsonInclude(JsonInclude.Include.NON_NULL)
data class GeoLocation(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,

    val placeName: String? = null,
    val countryCode: String? = null,

    val accuracyMeters: Int? = null
) : Serializable {
    fun hasCoordinates(): Boolean = latitude != null && longitude != null
}