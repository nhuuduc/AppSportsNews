package com.nhd.news.data.api

import com.nhd.news.data.models.Tag
import retrofit2.Response
import retrofit2.http.*

interface TagApiService {
    
    @GET("tags")
    suspend fun getAllTags(): Response<TagsResponse>
    
    @GET("tags/{id}")
    suspend fun getTagById(
        @Path("id") tagId: Int
    ): Response<TagDetailResponse>
}

data class TagsResponse(
    val success: Boolean,
    val data: List<Tag>?,
    val message: String? = null
)

data class TagDetailResponse(
    val success: Boolean,
    val data: TagDetail?,
    val message: String? = null
)

data class TagDetail(
    val tag: Tag,
    val articles: List<com.nhd.news.data.models.Article>
)

