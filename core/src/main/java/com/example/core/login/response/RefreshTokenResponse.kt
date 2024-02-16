package com.example.core.login.response

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponse( val accessToken: String)