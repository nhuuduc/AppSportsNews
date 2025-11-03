package com.nhd.news.data.api

import com.nhd.news.data.models.Article
import com.nhd.news.data.models.ArticlesResponse
import com.nhd.news.data.models.TrendingArticlesResponse
import com.nhd.news.data.models.Category
import com.nhd.news.data.models.Comment
import com.nhd.news.data.models.Team
import com.nhd.news.data.models.Match
import com.nhd.news.data.models.Video
import com.nhd.news.data.models.Tag
import com.nhd.news.data.models.User
import com.nhd.news.data.models.SearchResponse
import com.nhd.news.data.models.LikeResponse
import com.nhd.news.data.models.CommentResponse
import com.nhd.news.data.models.CommentLikeResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Categories
    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>
    
    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") categoryId: Int): Response<Category>
    
    // Articles
    @GET("articles")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category_id") categoryId: Int? = null
    ): Response<ArticleResponse>
    
    @GET("articles/{id}")
    suspend fun getArticleById(@Path("id") articleId: Int): Response<Article>
    
    @GET("articles/featured")
    suspend fun getFeaturedArticles(
        @Query("limit") limit: Int = 5
    ): Response<List<Article>>
    
    @GET("articles/breaking")
    suspend fun getBreakingNews(
        @Query("limit") limit: Int = 3
    ): Response<List<Article>>
    
    @GET("articles/trending")
    suspend fun getTrendingArticles(
        @Query("limit") limit: Int = 10
    ): Response<TrendingArticlesResponse>
    
    @GET("articles/category/{categoryId}")
    suspend fun getArticlesByCategory(
        @Path("categoryId") categoryId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ArticleResponse>
    
    // Article interactions
    @POST("articles/{id}/view")
    suspend fun trackArticleView(@Path("id") articleId: Int): Response<ViewTrackingResponse>
    
    @POST("articles/{id}/like")
    suspend fun toggleArticleLike(@Path("id") articleId: Int): Response<LikeResponse>
    
    @GET("articles/{id}/like-status")
    suspend fun getArticleLikeStatus(@Path("id") articleId: Int): Response<LikeStatusResponse>
    
    @GET("profile/favorites")
    suspend fun getLikedArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ArticlesResponse>
    
    // Comments
    @GET("articles/{id}/comments")
    suspend fun getCommentsByArticleId(@Path("id") articleId: Int): Response<List<Comment>>
    
    @POST("articles/{id}/comments")
    suspend fun addComment(@Path("id") articleId: Int, @Body request: CommentRequest): Response<CommentResponse>
    
    // Comment likes
    @POST("comments/{id}/like")
    suspend fun likeComment(@Path("id") commentId: Int): Response<CommentLikeResponse>
    
    @POST("comments/{id}/like")
    suspend fun unlikeComment(@Path("id") commentId: Int): Response<CommentLikeResponse>
    
    // Teams
    @GET("teams")
    suspend fun getTeams(@Query("category_id") categoryId: Int? = null): Response<List<Team>>
    
    @GET("teams/{id}")
    suspend fun getTeamById(@Path("id") teamId: Int): Response<Team>
    
    // Matches
    @GET("matches")
    suspend fun getMatches(
        @Query("category_id") categoryId: Int? = null,
        @Query("status") status: String? = null,
        @Query("date") date: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<List<Match>>
    
    @GET("matches/{id}")
    suspend fun getMatchById(@Path("id") matchId: Int): Response<Match>
    
    @GET("matches/live")
    suspend fun getLiveMatches(): Response<List<Match>>
    
    @GET("matches/upcoming")
    suspend fun getUpcomingMatches(
        @Query("limit") limit: Int = 10
    ): Response<List<Match>>
    
    // Videos
    @GET("videos")
    suspend fun getVideos(
        @Query("category_id") categoryId: Int? = null,
        @Query("match_id") matchId: Int? = null,
        @Query("limit") limit: Int = 20
    ): Response<List<Video>>
    
    @GET("videos/{id}")
    suspend fun getVideoById(@Path("id") videoId: Int): Response<Video>
    
    @GET("videos/highlights")
    suspend fun getHighlightVideos(
        @Query("limit") limit: Int = 10
    ): Response<List<Video>>
    
    // Tags
    @GET("tags")
    suspend fun getTags(): Response<List<Tag>>
    
    @GET("tags/{id}")
    suspend fun getTagById(@Path("id") tagId: Int): Response<Tag>
    
    @GET("articles/tags/{id}")
    suspend fun getArticlesByTag(
        @Path("id") tagId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ArticleResponse>
    
    // Search
    @GET("search")
    suspend fun searchContent(
        @Query("q") keyword: String,
        @Query("type") type: String = "all",
        @Query("limit") limit: Int = 20
    ): Response<SearchResponse>
}

data class ArticleResponse(
    val articles: List<Article>,
    val totalCount: Int,
    val currentPage: Int,
    val totalPages: Int
)

// Request/Response data classes for API interactions
data class ViewTrackingRequest(
    val article_id: Int
)

data class ViewTrackingResponse(
    val message: String,
    val view_count: Int
)

data class LikeRequest(
    val article_id: Int,
    val user_id: Int
)

data class CommentRequest(
    @SerializedName("article_id")
    val article_id: Int,
    @SerializedName("content")
    val content: String,
    @SerializedName("parent_comment_id")
    val parent_comment_id: Int? = null
)
