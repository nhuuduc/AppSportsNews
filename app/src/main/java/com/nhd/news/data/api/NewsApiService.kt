package com.nhd.news.data.api

import com.nhd.news.data.models.Article
import com.nhd.news.data.models.ArticlesResponse
import com.nhd.news.data.models.MatchesResponse
import com.nhd.news.data.models.VideosResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    
    // Articles endpoints
    @GET("articles")
    suspend fun getArticles(
        @Query("category") category: String = "sports",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ArticlesResponse>
    
    @GET("articles")
    suspend fun getArticleById(
        @Query("id") id: Int
    ): Response<Article>
    
    @GET("articles/search")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("category") category: String = "sports",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ArticlesResponse>
    
    @GET("articles/trending")
    suspend fun getTrendingArticles(
        @Query("limit") limit: Int = 10
    ): Response<ArticlesResponse>
    
    // Matches endpoints
    @GET("matches")
    suspend fun getMatches(
        @Query("date") date: String? = null,
        @Query("status") status: String? = null,
        @Query("competition") competition: String? = null
    ): Response<MatchesResponse>
    
    @GET("matches/live")
    suspend fun getLiveMatches(): Response<MatchesResponse>
    
    @GET("matches/today")
    suspend fun getTodayMatches(): Response<MatchesResponse>
    
    // Videos endpoints
    @GET("videos")
    suspend fun getVideos(
        @Query("category") category: String = "sports",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<VideosResponse>
    
    @GET("videos/search")
    suspend fun searchVideos(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<VideosResponse>
    
    @GET("videos/trending")
    suspend fun getTrendingVideos(
        @Query("limit") limit: Int = 10
    ): Response<VideosResponse>
}
