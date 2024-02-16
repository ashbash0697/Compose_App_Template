package com.example.core.data.models
import kotlinx.serialization.Serializable

@Serializable
data class BasePaginationTokenResponse<T>(
     val messages: List<T>,
     val nextPageToken: String? = null
)
