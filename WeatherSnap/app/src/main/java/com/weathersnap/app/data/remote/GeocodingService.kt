package com.weathersnap.app.data.remote

import com.weathersnap.app.data.remote.dto.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for the Open-Meteo Geocoding API.
 *
 * Base URL: https://geocoding-api.open-meteo.com/
 */
interface GeocodingService {

    /**
     * Search for cities by name.
     *
     * @param name    The city name to search for.
     * @param count   Maximum number of results to return (default 10).
     * @return [GeocodingResponse] containing matching [CityResult] entries.
     */
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10
    ): GeocodingResponse
}
