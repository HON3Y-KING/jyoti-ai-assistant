package com.jyoti.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Provides the Retrofit client used to reach OUR backend proxy.
 *
 * IMPORTANT — Security model:
 *  - `baseUrl` comes from BuildConfig.API_BASE_URL (set per-environment in local.properties),
 *    it is a public endpoint, not a secret.
 *  - There is NO Authorization header here containing an AI provider key (OpenAI/Anthropic/etc).
 *    Those keys live only on the backend server (see /server-example), which is the only
 *    thing allowed to call the AI provider.
 *  - If/when we add per-user auth, the app should attach a short-lived token obtained from
 *    our own auth flow (e.g. Firebase Auth ID token) here, NOT a static embedded secret,
 *    since static secrets embedded in an APK can always be extracted.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Body logging only in debug builds should be gated via BuildConfig.DEBUG in a real app.
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // .addInterceptor(AuthInterceptor(tokenProvider)) // add once real user auth exists
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, baseUrl: NetworkConfig): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl.apiBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)
}

/**
 * Small indirection so app/ passes BuildConfig.API_BASE_URL in without core:network
 * needing to depend on the app module (which would break the module boundary).
 */
data class NetworkConfig(val apiBaseUrl: String)

