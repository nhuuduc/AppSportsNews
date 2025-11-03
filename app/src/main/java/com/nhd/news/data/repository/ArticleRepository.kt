package com.nhd.news.data.repository

import com.nhd.news.data.api.ArticleApiService
import com.nhd.news.data.api.LikeStatusResponse
import com.nhd.news.data.models.LikeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val articleApiService: ArticleApiService
) {
    
    /**
     * Toggle article like (like/unlike)
     */
    fun toggleArticleLike(articleId: Int): Flow<Result<LikeResponse>> = flow {
        try {
            val response = articleApiService.toggleArticleLike(articleId)
            
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Không thể thích bài viết")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get article like status
     */
    fun getArticleLikeStatus(articleId: Int): Flow<Result<LikeStatusResponse>> = flow {
        try {
            val response = articleApiService.getArticleLikeStatus(articleId)
            
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Không thể lấy trạng thái thích")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}

