package com.weathersnap.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [WeatherReport] entities.
 */
@Dao
interface WeatherReportDao {

    /**
     * Insert a new weather report into the database.
     */
    @Insert
    suspend fun insert(report: WeatherReport)

    /**
     * Observe all saved weather reports, ordered newest-first.
     *
     * Returns a [Flow] so the UI layer receives automatic updates
     * whenever the underlying data changes.
     */
    @Query("SELECT * FROM weather_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<WeatherReport>>
}
