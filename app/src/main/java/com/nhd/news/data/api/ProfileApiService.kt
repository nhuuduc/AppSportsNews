package com.nhd.news.data.api

import com.nhd.news.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {
    
    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>
    
    @PUT("profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>
    
    @PUT("profile/password")
    suspend fun updatePassword(
        @Body request: UpdatePasswordRequest
    ): Response<ApiResponse>
    
    @GET("profile/favorites")
    suspend fun getFavorites(
        @Query("type") type: String = "all",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<FavoritesResponse>
}

data class UpdateProfileRequest(
    val full_name: String? = null,
    val phone: String? = null,
    val date_of_birth: String? = null,
    val gender: String? = null
)

data class UpdatePasswordRequest(
    val current_password: String,
    val new_password: String
)

data class ProfileResponse(
    val success: Boolean,
    val data: UserProfile?,
    val message: String? = null
)

data class UserProfile(
    val user_id: Int,
    val email: String,
    val username: String,
    val full_name: String?,
    val phone: String?,
    val avatar_url: String?,
    val date_of_birth: String?,
    val gender: String?,
    val is_verified: Boolean,
    val created_at: String,
    val updated_at: String
)

data class FavoritesResponse(
    val success: Boolean,
    val data: FavoritesPaginatedData?,
    val message: String? = null
)

data class FavoritesPaginatedData(
    val favorites: List<FavoriteDetail>,
    val pagination: PaginationInfo
)

data class FavoriteDetail(
    val favorite_id: Int,
    val type: String,
    val created_at: String,
    val article: Article? = null,
    val team: Team? = null
)
