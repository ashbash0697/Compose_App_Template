package com.example.core.data.models

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val data: T? = null,
    val successCode: String,
    val code: Int? = null,
    val message: String? = null
)