package com.nhd.news.data.api

import com.google.gson.annotations.SerializedName
import com.nhd.news.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ArticleApiService {
    
    @GET("articles")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category_id") categoryId: Int? = null
    ): Response<ArticlesResponse>
    
    @GET("articles/{id}")
    suspend fun getArticleById(
        @Path("id") articleId: Int
    ): Response<ArticleDetailResponse>
    
    @GET("articles/featured")
    suspend fun getFeaturedArticles(
        @Query("limit") limit: Int = 5
    ): Response<ArticleListResponse>
    
    @GET("articles/breaking")
    suspend fun getBreakingNews(
        @Query("limit") limit: Int = 3
    ): Response<ArticleListResponse>
    
    @GET("articles/trending")
    suspend fun getTrendingArticles(
        @Query("limit") limit: Int = 10
    ): Response<ArticleListResponse>
    
    @POST("articles/{id}/view")
    suspend fun incrementArticleView(
        @Path("id") articleId: Int
    ): Response<ViewCountResponse>
    
    @POST("articles/{id}/like")
    suspend fun toggleArticleLike(
        @Path("id") articleId: Int
    ): Response<LikeResponse>
    
    @GET("articles/{id}/like-status")
    suspend fun getArticleLikeStatus(
        @Path("id") articleId: Int
    ): Response<LikeStatusResponse>
    
    @GET("articles/{id}/comments")
    suspend fun getArticleComments(
        @Path("id") articleId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<CommentsResponse>
    
    @POST("articles/{id}/comments")
    suspend fun createComment(
        @Path("id") articleId: Int,
        @Body request: CreateCommentRequest
    ): Response<CommentResponse>
}

// Response Models
data class ArticleDetailResponse(
    val success: Boolean,
    val data: Article?,
    val message: String? = null
)

data class ArticleListResponse(
    val success: Boolean,
    val data: List<Article>?,
    val message: String? = null
)

data class ViewCountResponse(
    val success: Boolean,
    val message: String,
    val view_count: Int
)

data class LikeStatusResponse(
    val success: Boolean,
    @SerializedName("liked")
    val is_liked: Boolean,
    val like_count: Int
)

data class CommentsResponse(
    val success: Boolean,
    val data: CommentsPaginatedData?,
    val message: String? = null
)

data class CommentsPaginatedData(
    val comments: List<CommentItem>,
    val pagination: PaginationInfo
)

// Request Models
data class CreateCommentRequest(
    val content: String,
    val parent_comment_id: Int? = null
)

