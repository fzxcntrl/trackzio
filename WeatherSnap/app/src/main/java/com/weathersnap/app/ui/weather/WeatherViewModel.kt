package com.weathersnap.app.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.app.data.remote.WmoWeatherUtil
import com.weathersnap.app.data.remote.dto.CityResult
import com.weathersnap.app.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Weather screen.
 *
 * Manages the city search query, geocoding suggestions, and weather
 * data fetching.  All network calls run on [Dispatchers.IO].
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    // ── City search query ───────────────────────────────────────────

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // ── City suggestions from geocoding API ─────────────────────────

    private val _citySuggestions = MutableStateFlow<List<CityResult>>(emptyList())
    val citySuggestions: StateFlow<List<CityResult>> = _citySuggestions.asStateFlow()

    // ── Weather UI state ────────────────────────────────────────────

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    init {
        observeQuery()
    }

    // ── Public API ──────────────────────────────────────────────────

    /**
     * Called whenever the user types in the city search field.
     * Debounced and filtered in [observeQuery].
     */
    fun onQueryChanged(query: String) {
        _query.value = query
    }

    /**
     * Called when the user selects a city from the suggestion list.
     * Triggers a weather fetch for that city's coordinates.
     */
    fun onCitySelected(city: CityResult) {
        _citySuggestions.value = emptyList()
        _query.value = city.name

        viewModelScope.launch(Dispatchers.IO) {
            _weatherState.value = WeatherUiState.Loading
            try {
                val response = repository.getWeather(city.latitude, city.longitude)
                val current = response.currentWeather

                if (current == null) {
                    _weatherState.value = WeatherUiState.Empty
                    return@launch
                }

                val humidity = response.hourly?.relativeHumidity?.getOrNull(0) ?: 0
                val pressure = response.hourly?.surfacePressure?.getOrNull(0) ?: 0.0

                val weatherData = WeatherData(
                    cityName = city.name,
                    country = city.country,
                    temperature = current.temperature,
                    condition = WmoWeatherUtil.getConditionLabel(current.weathercode),
                    humidity = humidity,
                    windspeed = current.windspeed,
                    pressure = pressure
                )

                _weatherState.value = WeatherUiState.Success(weatherData)
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(
                    e.localizedMessage ?: "Failed to fetch weather data"
                )
            }
        }
    }

    // ── Internal ────────────────────────────────────────────────────

    /**
     * Observes the [_query] flow with a 300 ms debounce.
     * Only triggers a geocoding search when the query length > 2.
     */
    private fun observeQuery() {
        _query
            .debounce(300L)
            .distinctUntilChanged()
            .filter { it.length > 2 }
            .onEach { q ->
                try {
                    val results = repository.searchCities(q)
                    _citySuggestions.value = results
                } catch (e: Exception) {
                    _citySuggestions.value = emptyList()
                }
            }
            .launchIn(viewModelScope)
    }
}
