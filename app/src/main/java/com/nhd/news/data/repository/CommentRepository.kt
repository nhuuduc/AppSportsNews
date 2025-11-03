package com.nhd.news.data.repository

import com.nhd.news.data.api.ArticleApiService
import com.nhd.news.data.api.CommentApiService
import com.nhd.news.data.api.CreateCommentRequest
import com.nhd.news.data.models.CommentItem
import com.nhd.news.data.models.CommentLikeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val articleApiService: ArticleApiService,
    private val commentApiService: CommentApiService
) {
    
    /**
     * Get comments for an article
     */
    fun getComments(articleId: Int, page: Int = 1, limit: Int = 20): Flow<Result<List<CommentItem>>> = flow {
        try {
            val response = articleApiService.getArticleComments(articleId, page, limit)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    emit(Result.success(body.data.comments))
                } else {
                    emit(Result.success(emptyList()))
                }
            } else {
                emit(Result.success(emptyList()))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Post a comment
     */
    fun postComment(articleId: Int, content: String, parentCommentId: Int? = null): Flow<Result<CommentItem>> = flow<Result<CommentItem>> {
        try {
            val request = CreateCommentRequest(content, parentCommentId)
            val response = articleApiService.createComment(articleId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.comment != null) {
                    emit(Result.success(body.comment))
                } else {
                    emit(Result.failure(Exception(body.message ?: "Đăng bình luận thất bại")))
                }
            } else {
                emit(Result.failure(Exception("Đăng bình luận thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Toggle comment like (like/unlike)
     */
    fun toggleCommentLike(commentId: Int): Flow<Result<CommentLikeResponse>> = flow {
        try {
            val response = commentApiService.toggleCommentLike(commentId)
            
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Không thể thích bình luận")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}

