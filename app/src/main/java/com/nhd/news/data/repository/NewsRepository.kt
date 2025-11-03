package com.nhd.news.data.repository

import androidx.paging.PagingData
import com.nhd.news.data.models.Article
import com.nhd.news.data.models.Match
import com.nhd.news.data.models.Video
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    // Articles (Legacy - không dùng Paging)
    fun getArticles(category: String = "sports", page: Int = 1): Flow<Result<List<Article>>>

    fun getArticleById(id: Int): Flow<Result<Article>>

    fun searchArticles(query: String, page: Int = 1): Flow<Result<List<Article>>>

    fun getTrendingArticles(): Flow<Result<List<Article>>>
    
    // Articles với Paging 3
    fun getArticlesPaged(categoryId: Int? = null): Flow<PagingData<Article>>
    
    fun searchArticlesPaged(query: String): Flow<PagingData<Article>>

    // Matches
    fun getMatches(date: String? = null, status: String? = null): Flow<Result<List<Match>>>

    fun getLiveMatches(): Flow<Result<List<Match>>>

    fun getTodayMatches(): Flow<Result<List<Match>>>

    // Videos
    fun getVideos(category: String = "sports", page: Int = 1): Flow<Result<List<Video>>>

    fun searchVideos(query: String, page: Int = 1): Flow<Result<List<Video>>>

    fun getTrendingVideos(): Flow<Result<List<Video>>>
    
    // Article interactions
    fun incrementArticleView(articleId: Int): Flow<Result<Unit>>
}