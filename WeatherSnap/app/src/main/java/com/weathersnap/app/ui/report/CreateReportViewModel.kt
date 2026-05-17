package com.weathersnap.app.ui.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.app.data.local.WeatherReport
import com.weathersnap.app.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One-shot event emitted after a report is saved successfully.
 */
sealed class CreateReportEvent {
    data object SaveSuccess : CreateReportEvent()
}

/**
 * ViewModel for the Create Report screen.
 *
 * Holds the captured photo metadata and user notes, and
 * delegates persistence to [ReportRepository].
 */
@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    // ── Photo state ─────────────────────────────────────────────

    var photoPath by mutableStateOf<String?>(null)
        private set

    var originalSizeKb by mutableLongStateOf(0L)
        private set

    var compressedSizeKb by mutableLongStateOf(0L)
        private set

    // ── Notes ────────────────────────────────────────────────────

    var notes by mutableStateOf("")
        private set

    // ── One-shot events ─────────────────────────────────────────

    private val _events = MutableSharedFlow<CreateReportEvent>()
    val events: SharedFlow<CreateReportEvent> = _events.asSharedFlow()

    // ── Public API ──────────────────────────────────────────────

    /**
     * Called when the CameraScreen returns a captured photo result.
     */
    fun onPhotoResult(path: String, originalKb: Long, compressedKb: Long) {
        photoPath = path
        originalSizeKb = originalKb
        compressedSizeKb = compressedKb
    }

    /**
     * Update the user notes.
     */
    fun onNotesChanged(value: String) {
        notes = value
    }

    /**
     * Persist the weather report to the local database.
     *
     * Emits [CreateReportEvent.SaveSuccess] on completion so the
     * screen can navigate away.
     */
    fun saveReport(
        cityName: String,
        country: String,
        temperature: Double,
        condition: String,
        humidity: Double,
        windspeed: Double,
        pressure: Double
    ) {
        val path = photoPath ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val report = WeatherReport(
                cityName = cityName,
                country = country,
                condition = condition,
                temperature = temperature,
                humidity = humidity,
                windspeed = windspeed,
                pressure = pressure,
                imagePath = path,
                originalSizeKb = originalSizeKb,
                compressedSizeKb = compressedSizeKb,
                notes = notes
            )
            reportRepository.saveReport(report)
            _events.emit(CreateReportEvent.SaveSuccess)
        }
    }
}
