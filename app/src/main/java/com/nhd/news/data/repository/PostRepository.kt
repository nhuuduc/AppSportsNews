package com.nhd.news.data.repository

import com.nhd.news.data.api.PostApiService
import com.nhd.news.data.api.MyPostsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postApiService: PostApiService
) {
    
    /**
     * Create new post
     */
    fun createPost(
        title: String,
        content: String,
        categoryId: Int
    ): Flow<Result<String>> = flow {
        try {
            val json = JSONObject().apply {
                put("title", title)
                put("content", content)
                put("category_id", categoryId)
            }
            
            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val response = postApiService.createPost(body)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.success) {
                    emit(Result.success(responseBody.message))
                } else {
                    emit(Result.failure(Exception(responseBody.message)))
                }
            } else {
                emit(Result.failure(Exception("Tạo bài viết thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get my posts
     */
    fun getMyPosts(page: Int = 1, limit: Int = 20): Flow<Result<MyPostsResponse>> = flow {
        try {
            val response = postApiService.getMyPosts(page, limit)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.success) {
                    emit(Result.success(responseBody))
                } else {
                    emit(Result.failure(Exception(responseBody.message)))
                }
            } else {
                emit(Result.failure(Exception("Tải bài viết thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Delete post
     */
    fun deletePost(articleId: Int): Flow<Result<String>> = flow {
        try {
            val response = postApiService.deletePost(articleId)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.success) {
                    emit(Result.success(responseBody.message))
                } else {
                    emit(Result.failure(Exception(responseBody.message)))
                }
            } else {
                emit(Result.failure(Exception("Xóa bài viết thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Upload image for post
     */
    fun uploadImage(imageFile: File): Flow<Result<String>> = flow {
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            
            val response = postApiService.uploadImage(body)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.success && responseBody.url != null) {
                    emit(Result.success(responseBody.url))
                } else {
                    emit(Result.failure(Exception(responseBody.message)))
                }
            } else {
                emit(Result.failure(Exception("Upload ảnh thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}

