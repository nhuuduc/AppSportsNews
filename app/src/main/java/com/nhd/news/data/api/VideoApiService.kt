package com.nhd.news.data.api

import com.nhd.news.data.models.Video
import retrofit2.Response
import retrofit2.http.*

interface VideoApiService {
    
    @GET("videos")
    suspend fun getVideos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<VideosResponse>
    
    @GET("videos/{id}")
    suspend fun getVideoById(
        @Path("id") videoId: Int
    ): Response<VideoDetailResponse>
    
    @GET("videos/highlights")
    suspend fun getHighlightVideos(
        @Query("limit") limit: Int = 10
    ): Response<VideoListResponse>
}

data class VideosResponse(
    val success: Boolean,
    val data: VideosPaginatedData?,
    val message: String? = null
)

data class VideosPaginatedData(
    val videos: List<Video>,
    val pagination: PaginationInfo
)

data class VideoDetailResponse(
    val success: Boolean,
    val data: Video?,
    val message: String? = null
)

data class VideoListResponse(
    val success: Boolean,
    val data: List<Video>?,
    val message: String? = null
)
