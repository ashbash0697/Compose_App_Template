package com.example.core.network

import android.os.Build
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

object BaseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        with(request) {
            addMetaHeaders(this)
        }
        return chain.proceed(request.build())
    }

    private fun addMetaHeaders(request: Request.Builder) {
        request.addHeader("os", "android")
        request.addHeader("osversion", "${Build.VERSION.SDK_INT}")
        request.addHeader("model", "${Build.BRAND} ${Build.MODEL} ${Build.ID}")
        //request.addHeader("appversion", "${BuildConfig.versionCode}")
    }
}