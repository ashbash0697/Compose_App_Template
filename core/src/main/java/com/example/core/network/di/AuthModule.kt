package com.example.core.network.di

import com.example.core.network.getRetrofit
import com.holofy.core.network.AuthRefreshService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
class AuthModule {

    @Provides
    fun providesAuthRefreshService(
        okHttpClient: OkHttpClient,
        json: Json
    ): AuthRefreshService = getRetrofit(json, okHttpClient)
        .create(AuthRefreshService::class.java)


}