package com.weathersnap.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.app.data.local.WeatherReport
import com.weathersnap.app.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the Saved Reports screen.
 *
 * Exposes a [StateFlow] of all persisted [WeatherReport] entries,
 * sourced from Room via [ReportRepository] and shared using
 * [SharingStarted.WhileSubscribed] to stop collection when the
 * screen is off-screen for more than 5 seconds.
 */
@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    reportRepository: ReportRepository
) : ViewModel() {

    val reports: StateFlow<List<WeatherReport>> = reportRepository
        .getReports()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )
}
