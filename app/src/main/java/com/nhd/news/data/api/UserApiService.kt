package com.nhd.news.data.api

import com.nhd.news.data.models.User
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    
    @GET("profile")
    suspend fun getProfile(): Response<UserProfileResponse>
    
    @PUT("profile")
    suspend fun updateProfile(@Body body: RequestBody): Response<BaseResponse>
    
    @Multipart
    @POST("profile/avatar")
    suspend fun updateAvatar(@Part avatar: okhttp3.MultipartBody.Part): Response<AvatarResponse>
    
    @PUT("profile/password")
    suspend fun changePassword(@Body body: RequestBody): Response<BaseResponse>
}

data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val profile: User?
)

data class BaseResponse(
    val success: Boolean,
    val message: String
)

data class AvatarResponse(
    val success: Boolean,
    val message: String,
    val avatar_url: String?
)

