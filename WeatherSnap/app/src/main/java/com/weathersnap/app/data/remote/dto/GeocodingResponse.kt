package com.weathersnap.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Top-level response from the Open-Meteo Geocoding API.
 *
 * Example endpoint:
 * GET https://geocoding-api.open-meteo.com/v1/search?name=London&count=10
 */
data class GeocodingResponse(
    @SerializedName("results")
    val results: List<CityResult>? = null
)

/**
 * A single city result returned by the geocoding search.
 */
data class CityResult(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)
