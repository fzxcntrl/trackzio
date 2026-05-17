package com.weathersnap.app.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Sealed class defining all navigation routes in WeatherSnap.
 * Each route maps to a distinct screen destination.
 */
sealed class Screen(val route: String) {

    /** Home screen showing current weather data */
    data object Weather : Screen("weather")

    /** Screen for creating a new weather report with photo + notes */
    data object CreateReport : Screen(
        "create_report/{$ARG_CITY}/{$ARG_COUNTRY}/{$ARG_TEMPERATURE}/{$ARG_CONDITION}/{$ARG_HUMIDITY}/{$ARG_WINDSPEED}/{$ARG_PRESSURE}"
    ) {
        /**
         * Build a concrete route with all weather parameters URL-encoded.
         */
        fun createRoute(
            cityName: String,
            country: String,
            temperature: Double,
            condition: String,
            humidity: Int,
            windspeed: Double,
            pressure: Double
        ): String {
            val enc = { s: String -> URLEncoder.encode(s, StandardCharsets.UTF_8.toString()) }
            return "create_report/${enc(cityName)}/${enc(country)}/$temperature/${enc(condition)}/$humidity/$windspeed/$pressure"
        }
    }

    /** Full-screen camera capture screen */
    data object Camera : Screen("camera")

    /** Screen listing all previously saved weather reports */
    data object SavedReports : Screen("saved_reports")

    companion object {
        const val ARG_CITY = "cityName"
        const val ARG_COUNTRY = "country"
        const val ARG_TEMPERATURE = "temperature"
        const val ARG_CONDITION = "condition"
        const val ARG_HUMIDITY = "humidity"
        const val ARG_WINDSPEED = "windspeed"
        const val ARG_PRESSURE = "pressure"
    }
}

