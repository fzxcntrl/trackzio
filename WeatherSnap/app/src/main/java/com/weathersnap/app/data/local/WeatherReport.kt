package com.weathersnap.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a saved weather report.
 *
 * Each report captures a snapshot of weather conditions at a
 * specific location, along with a camera-captured photo that
 * has been compressed for storage.
 */
@Entity(tableName = "weather_reports")
data class WeatherReport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val cityName: String,
    val country: String,
    val condition: String,

    val temperature: Double,
    val humidity: Double,
    val windspeed: Double,
    val pressure: Double,

    /** Absolute file path to the compressed image on disk. */
    val imagePath: String,

    /** Original image size before compression, in kilobytes. */
    val originalSizeKb: Long,

    /** Compressed image size after compression, in kilobytes. */
    val compressedSizeKb: Long,

    /** User-entered notes for the report. */
    val notes: String,

    /** Epoch millis when the report was created. */
    val timestamp: Long = System.currentTimeMillis()
)
