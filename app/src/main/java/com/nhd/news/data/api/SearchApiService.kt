package com.nhd.news.data.api

import com.nhd.news.data.models.SearchResponse
import retrofit2.Response
import retrofit2.http.*

interface SearchApiService {
    
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "all",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<SearchResponse>
}

