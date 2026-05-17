package com.weathersnap.app.ui.weather

/**
 * Sealed class representing every possible state of the Weather screen.
 */
sealed class WeatherUiState {

    /** No action has been taken yet. */
    data object Idle : WeatherUiState()

    /** A network request is in progress. */
    data object Loading : WeatherUiState()

    /** Weather data was fetched successfully. */
    data class Success(val data: WeatherData) : WeatherUiState()

    /** An error occurred during the fetch. */
    data class Error(val message: String) : WeatherUiState()

    /** The API returned a valid response but no usable data. */
    data object Empty : WeatherUiState()
}
