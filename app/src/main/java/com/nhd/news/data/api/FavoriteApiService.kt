package com.nhd.news.data.api

import com.nhd.news.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface FavoriteApiService {
    
    @POST("favorites")
    suspend fun addToFavorites(
        @Body request: AddFavoriteRequest
    ): Response<FavoriteResponse>
    
    @DELETE("favorites/{id}")
    suspend fun removeFromFavorites(
        @Path("id") favoriteId: Int
    ): Response<ApiResponse>
}

data class AddFavoriteRequest(
    val type: String,
    val article_id: Int? = null,
    val team_id: Int? = null
)

data class FavoriteResponse(
    val success: Boolean,
    val message: String,
    val data: FavoriteItem? = null
)

data class FavoriteItem(
    val favorite_id: Int,
    val user_id: Int,
    val type: String,
    val article_id: Int?,
    val team_id: Int?,
    val created_at: String
)

