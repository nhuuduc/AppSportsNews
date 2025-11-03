package com.nhd.news.data.repository

import com.nhd.news.data.api.ApiService
import com.nhd.news.data.models.LikeResponse
import com.nhd.news.data.models.CommentLikeResponse
import com.nhd.news.data.models.ArticlesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikeRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * Toggle article like (like/unlike)
     */
    fun toggleArticleLike(articleId: Int): Flow<Result<LikeResponse>> = flow {
        try {
            val response = apiService.toggleArticleLike(articleId)
            
            if (response.isSuccessful) {
                response.body()?.let { likeResponse ->
                    emit(Result.success(likeResponse))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Bạn cần đăng nhập"
                    403 -> "Bạn cần xác thực email trước"
                    404 -> "Bài viết không tồn tại"
                    else -> "Không thể thực hiện thao tác (${response.code()})"
                }
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Lỗi kết nối: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Like a comment
     */
    fun likeComment(commentId: Int): Flow<Result<CommentLikeResponse>> = flow {
        try {
            val response = apiService.likeComment(commentId)
            
            if (response.isSuccessful) {
                response.body()?.let { likeResponse ->
                    emit(Result.success(likeResponse))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Bạn đã thích bình luận này rồi"
                    401 -> "Bạn cần đăng nhập"
                    403 -> "Bạn cần xác thực email trước"
                    404 -> "Bình luận không tồn tại"
                    else -> "Không thể thích bình luận (${response.code()})"
                }
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Lỗi kết nối: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Unlike a comment
     */
    fun unlikeComment(commentId: Int): Flow<Result<CommentLikeResponse>> = flow {
        try {
            val response = apiService.unlikeComment(commentId)
            
            if (response.isSuccessful) {
                response.body()?.let { likeResponse ->
                    emit(Result.success(likeResponse))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Bạn chưa thích bình luận này"
                    401 -> "Bạn cần đăng nhập"
                    404 -> "Bình luận không tồn tại"
                    else -> "Không thể bỏ thích bình luận (${response.code()})"
                }
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Lỗi kết nối: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get article like status
     */
    fun getArticleLikeStatus(articleId: Int): Flow<Result<Pair<Boolean, Int>>> = flow {
        try {
            val response = apiService.getArticleLikeStatus(articleId)
            
            if (response.isSuccessful) {
                response.body()?.let { statusResponse ->
                    android.util.Log.d("LikeRepository", "Article $articleId like status: isLiked=${statusResponse.is_liked}, likeCount=${statusResponse.like_count}")
                    // Return Pair of (isLiked, likeCount)
                    emit(Result.success(Pair(statusResponse.is_liked, statusResponse.like_count)))
                } ?: run {
                    android.util.Log.e("LikeRepository", "Empty response body for article $articleId like status")
                    emit(Result.failure(Exception("Empty response body")))
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Bạn cần đăng nhập"
                    404 -> "Bài viết không tồn tại"
                    else -> "Không thể lấy trạng thái like (${response.code()})"
                }
                android.util.Log.e("LikeRepository", "Failed to get like status for article $articleId: $errorMsg")
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            android.util.Log.e("LikeRepository", "Exception getting like status for article $articleId: ${e.message}", e)
            emit(Result.failure(Exception("Lỗi kết nối: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get user's liked (saved) articles
     */
    fun getLikedArticles(page: Int = 1, limit: Int = 20): Flow<Result<ArticlesResponse>> = flow {
        try {
            val response = apiService.getLikedArticles(page, limit)
            
            if (response.isSuccessful) {
                response.body()?.let { articlesResponse ->
                    emit(Result.success(articlesResponse))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Bạn cần đăng nhập"
                    403 -> "Bạn cần xác thực email trước"
                    else -> "Không thể tải bài viết đã like (${response.code()})"
                }
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Lỗi kết nối: ${e.message}")))
        }
    }.flowOn(Dispatchers.IO)
}

