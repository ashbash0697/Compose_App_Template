package com.example.core.data.models

import kotlinx.serialization.Serializable

@Serializable
data class BasePaginationResponse<T>(
    val docs: List<T>,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val page: Int,
    val pagingCounter: Int,
    val totalDocs: Int,
    val totalPages: Int
)