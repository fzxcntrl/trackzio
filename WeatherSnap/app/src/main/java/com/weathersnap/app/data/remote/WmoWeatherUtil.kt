package com.weathersnap.app.data.remote

/**
 * Utility for mapping WMO (World Meteorological Organization) weather
 * interpretation codes to human-readable condition labels.
 *
 * Reference: https://open-meteo.com/en/docs → "WMO Weather interpretation codes"
 */
object WmoWeatherUtil {

    /**
     * Returns a human-readable weather condition string for the given
     * WMO weather code.
     *
     * @param code WMO weather interpretation code from the API.
     * @return A short label describing the weather condition.
     */
    fun getConditionLabel(code: Int): String = when (code) {
        0          -> "Clear sky"
        1, 2, 3    -> "Partly cloudy"
        45, 48     -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Showers"
        95         -> "Thunderstorm"
        else       -> "Unknown"
    }
}
