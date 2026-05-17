package com.weathersnap.app.data.repository

import com.weathersnap.app.data.remote.GeocodingService
import com.weathersnap.app.data.remote.WeatherService
import com.weathersnap.app.data.remote.dto.CityResult
import com.weathersnap.app.data.remote.dto.WeatherResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that mediates between the remote data sources
 * ([GeocodingService], [WeatherService]) and the rest of the app.
 *
 * Provides an in-memory cache for city search results to avoid
 * redundant network calls for repeated queries.
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val geocodingService: GeocodingService,
    private val weatherService: WeatherService
) {

    /** In-memory cache keyed by the lowercase search query. */
    private val cityCache = HashMap<String, List<CityResult>>()

    /**
     * Search for cities matching [query].
     *
     * Returns a cached result if available; otherwise calls the
     * Geocoding API, caches the response, and returns it.
     *
     * @param query City name to search for.
     * @return List of matching [CityResult] entries (may be empty).
     */
    suspend fun searchCities(query: String): List<CityResult> {
        val key = query.lowercase().trim()

        cityCache[key]?.let { cached ->
            return cached
        }

        val results = geocodingService.searchCity(name = query).results.orEmpty()
        cityCache[key] = results
        return results
    }

    /**
     * Fetch the current weather forecast for the given coordinates.
     *
     * @param lat Latitude of the location.
     * @param lon Longitude of the location.
     * @return [WeatherResponse] with current conditions and hourly arrays.
     */
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return weatherService.getWeather(latitude = lat, longitude = lon)
    }
}
