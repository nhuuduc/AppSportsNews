package com.nhd.news.data.api

data class PaginationInfo(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class ApiResponse(
    val success: Boolean,
    val message: String? = null
)
