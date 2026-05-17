package com.weathersnap.app.data.remote

import com.weathersnap.app.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for the Open-Meteo Forecast API.
 *
 * Base URL: https://api.open-meteo.com/
 */
interface WeatherService {

    /**
     * Fetch weather forecast for the given coordinates.
     *
     * @param latitude        Latitude of the location.
     * @param longitude       Longitude of the location.
     * @param currentWeather  Whether to include current weather (default true).
     * @param hourly          Comma-separated list of hourly variables to return.
     * @return [WeatherResponse] containing current weather conditions.
     */
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "relativehumidity_2m,surface_pressure,windspeed_10m"
    ): WeatherResponse
}
