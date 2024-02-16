package com.example.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Lazy
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

fun getRetrofit(json: Json,okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    //.baseUrl(BuildConfig.baseUrl)
    .client(okHttpClient)
    .build()

fun getRetrofit(json: Json, client: Lazy<OkHttpClient>): Retrofit {
    return Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        //.baseUrl(BuildConfig.baseUrl)
        .callFactory(client.get())
        .build()
}