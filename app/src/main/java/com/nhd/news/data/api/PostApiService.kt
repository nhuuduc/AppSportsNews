package com.nhd.news.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PostApiService {
    
    @POST("posts/create")
    suspend fun createPost(@Body body: RequestBody): Response<PostResponse>
    
    @POST("posts/update")
    suspend fun updatePost(@Body body: RequestBody): Response<PostResponse>
    
    @DELETE("posts/delete")
    suspend fun deletePost(@Query("article_id") articleId: Int): Response<PostResponse>
    
    @GET("posts/my-posts")
    suspend fun getMyPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<MyPostsResponse>
    
    @Multipart
    @POST("posts/upload-image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>
}

data class PostResponse(
    val success: Boolean,
    val message: String,
    val article_id: Int?
)

data class MyPostsResponse(
    val success: Boolean,
    val message: String,
    val articles: List<Any>?,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class ImageUploadResponse(
    val success: Boolean,
    val message: String,
    val url: String?,
    val filename: String?
)

