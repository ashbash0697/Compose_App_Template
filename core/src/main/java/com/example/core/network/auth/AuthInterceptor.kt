package com.example.core.network.auth


import com.example.core.login.datasource.AuthTokenLocalDatasource
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.*
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authTokenLocalDatasource: AuthTokenLocalDatasource
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenData = runBlocking {
             authTokenLocalDatasource.getTokenData()
        }

        val request = chain.request().newBuilder()
        with(request) {
            if (chain.request().header("Authorization").isNullOrBlank()) {
                addHeader("Authorization", "Bearer ${tokenData.authToken}")
            }
            //addMetaHeaders(this, tokenData)

            addHeader("Accept-Language", Locale.getDefault().toLanguageTag())
        }
        return chain.proceed(request.build())
    }

    /*private fun addMetaHeaders(request: Request.Builder) {
        request.addHeader("userid", "") authTokenLocalDatasource.userId
    }*/
}