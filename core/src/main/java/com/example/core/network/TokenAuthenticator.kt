package com.example.core.network

import android.util.Log
import com.example.core.data.NoInternetException
import com.example.core.login.datasource.AuthTokenLocalDatasource
import com.example.core.login.request.RefreshTokenRequest
import com.holofy.core.network.AuthRefreshService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val authRefreshService: AuthRefreshService,
    private val authTokenLocalDatasource: AuthTokenLocalDatasource
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        runBlocking {
            authTokenLocalDatasource.getRefreshToken()?.let { refreshToken ->
                val refreshCall = authRefreshService.performTokenRefresh(
                    //userId = authTokenLocalDatasource.userId,
                    refreshTokenRequest = RefreshTokenRequest(
                        refreshToken
                    )
                )
                try {
                    val refreshResponse = refreshCall.execute()
                    if (refreshResponse.isSuccessful) {
                        Log.e(
                            "refreshSuccessful",
                            "token is ${refreshResponse.body()?.data?.accessToken}"
                        )
                         refreshResponse.body()?.data?.accessToken?.let {

                         }
                        refreshResponse.body()?.data?.accessToken?.let {
                            authTokenLocalDatasource.updateAuthTokens(authToken = it)
                        }

                        return@runBlocking response.request.newBuilder()
                            .header(
                                "Authorization",
                                "Bearer ${refreshResponse.body()?.data?.accessToken}"
                            )
                            .build()
                    }
                } catch (nie: NoInternetException) {
                    return@runBlocking null
                } catch (e: Exception) {
                    return@runBlocking null
                }
            }
        }
        return null
    }

}