package com.example.core.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(val token: String)