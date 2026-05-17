package com.weathersnap.app.ui.weather

/**
 * Immutable data class holding the weather information
 * displayed on the Weather screen.
 */
data class WeatherData(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windspeed: Double,
    val pressure: Double
)
