package com.weathersnap.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module providing application-scoped dependencies.
 *
 * Networking bindings (OkHttp, Retrofit, API services) live in
 * [com.weathersnap.app.data.remote.NetworkModule].
 *
 * Room database and CameraX bindings will be added alongside their
 * respective feature implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Future non-network bindings (Room database, repositories, etc.) go here.
}
