package com.weathersnap.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Top-level response from the Open-Meteo Forecast API.
 *
 * Example endpoint:
 * GET https://api.open-meteo.com/v1/forecast?latitude=51.5&longitude=-0.12
 *     &current_weather=true
 *     &hourly=relativehumidity_2m,surface_pressure,windspeed_10m
 */
data class WeatherResponse(
    @SerializedName("current_weather")
    val currentWeather: CurrentWeather? = null,

    @SerializedName("hourly")
    val hourly: HourlyData? = null
)

/**
 * Current weather conditions returned inside [WeatherResponse].
 */
data class CurrentWeather(
    @SerializedName("temperature")
    val temperature: Double,

    @SerializedName("windspeed")
    val windspeed: Double,

    @SerializedName("weathercode")
    val weathercode: Int
)

/**
 * Hourly forecast arrays returned inside [WeatherResponse].
 * Each list is indexed by hour offset from the forecast start.
 */
data class HourlyData(
    @SerializedName("relativehumidity_2m")
    val relativeHumidity: List<Int>? = null,

    @SerializedName("surface_pressure")
    val surfacePressure: List<Double>? = null,

    @SerializedName("windspeed_10m")
    val windspeed10m: List<Double>? = null
)
