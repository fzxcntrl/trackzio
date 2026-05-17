package com.weathersnap.app.data.repository

import com.weathersnap.app.data.local.WeatherReport
import com.weathersnap.app.data.local.WeatherReportDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that mediates access to the local [WeatherReport] store.
 *
 * Writes run on [Dispatchers.IO]; reads are returned as a [Flow]
 * whose collection context is determined by the caller.
 */
@Singleton
class ReportRepository @Inject constructor(
    private val dao: WeatherReportDao
) {

    /**
     * Persist a new weather report to the local database.
     * Runs the insert on [Dispatchers.IO].
     */
    suspend fun saveReport(report: WeatherReport) {
        withContext(Dispatchers.IO) {
            dao.insert(report)
        }
    }

    /**
     * Observe all saved weather reports, ordered newest-first.
     */
    fun getReports(): Flow<List<WeatherReport>> = dao.getAllReports()
}
