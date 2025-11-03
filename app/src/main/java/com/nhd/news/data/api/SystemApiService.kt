package com.nhd.news.data.api

import retrofit2.Response
import retrofit2.http.*

interface SystemApiService {
    
    @GET("/")
    suspend fun getApiInfo(): Response<ApiInfoResponse>
    
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}

data class ApiInfoResponse(
    val success: Boolean,
    val name: String,
    val version: String,
    val message: String? = null
)

data class HealthResponse(
    val success: Boolean,
    val status: String,
    val timestamp: String,
    val message: String? = null
)

