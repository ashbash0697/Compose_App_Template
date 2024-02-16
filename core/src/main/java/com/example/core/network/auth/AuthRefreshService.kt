package com.holofy.core.network

import com.example.core.data.models.BaseResponse
import com.example.core.login.request.RefreshTokenRequest
import com.example.core.login.response.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthRefreshService {
    @POST("dummy/url")
    fun performTokenRefresh(
        //@Header("userid") userId: String,
        @Body refreshTokenRequest: RefreshTokenRequest
    ): Call<BaseResponse<RefreshTokenResponse>>
}