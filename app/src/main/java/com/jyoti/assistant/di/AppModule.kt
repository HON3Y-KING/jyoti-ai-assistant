package com.jyoti.assistant.di

import com.jyoti.assistant.BuildConfig
import com.jyoti.core.network.NetworkConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Only the app module reads BuildConfig (it's the only module that should know about
 * build variants/environments). It hands the resolved, non-secret proxy URL down into
 * core:network. This keeps core:network reusable and free of app-specific config.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig = NetworkConfig(apiBaseUrl = BuildConfig.API_BASE_URL)
}
