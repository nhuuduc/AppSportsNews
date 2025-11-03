package com.nhd.news.data.api

import com.nhd.news.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface CommentApiService {
    
    @PUT("comments/{id}")
    suspend fun updateComment(
        @Path("id") commentId: Int,
        @Body request: UpdateCommentRequest
    ): Response<CommentResponse>
    
    @DELETE("comments/{id}")
    suspend fun deleteComment(
        @Path("id") commentId: Int
    ): Response<ApiResponse>
    
    @POST("comments/{id}/like")
    suspend fun toggleCommentLike(
        @Path("id") commentId: Int
    ): Response<CommentLikeResponse>
}

data class UpdateCommentRequest(
    val content: String
)

