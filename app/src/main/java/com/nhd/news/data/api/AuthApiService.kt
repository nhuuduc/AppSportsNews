package com.nhd.news.data.api

import com.nhd.news.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<SessionVerifyResponse>
    
    @POST("auth/verify")
    suspend fun verifyEmail(
        @Body request: VerifyEmailRequest
    ): Response<VerifyEmailResponse>
    
    @POST("auth/resend-verification")
    suspend fun resendVerification(
        @Body request: ResendVerificationRequest
    ): Response<ApiResponse>
    
    @GET("auth/verify-session")
    suspend fun verifySession(
        @Query("token") token: String
    ): Response<SessionVerifyResponse>
}

data class VerifyEmailRequest(
    val token: String
)

data class ResendVerificationRequest(
    val email: String
)
