package com.weathersnap.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for WeatherSnap.
 *
 * Bump [version] and provide a migration strategy when the
 * schema changes in future releases.
 */
@Database(
    entities = [WeatherReport::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherSnapDatabase : RoomDatabase() {

    /** Provides access to the [WeatherReportDao]. */
    abstract fun weatherReportDao(): WeatherReportDao
}
