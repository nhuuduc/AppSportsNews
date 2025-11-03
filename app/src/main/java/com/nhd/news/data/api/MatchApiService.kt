package com.nhd.news.data.api

import com.nhd.news.data.models.Match
import retrofit2.Response
import retrofit2.http.*

interface MatchApiService {
    
    @GET("matches")
    suspend fun getMatches(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null,
        @Query("category_id") categoryId: Int? = null
    ): Response<MatchesResponse>
    
    @GET("matches/{id}")
    suspend fun getMatchById(
        @Path("id") matchId: Int
    ): Response<MatchDetailResponse>
    
    @GET("matches/live")
    suspend fun getLiveMatches(
        @Query("limit") limit: Int = 20
    ): Response<MatchListResponse>
    
    @GET("matches/upcoming")
    suspend fun getUpcomingMatches(
        @Query("limit") limit: Int = 20
    ): Response<MatchListResponse>
}

// Response Models
data class MatchesResponse(
    val success: Boolean,
    val data: MatchesPaginatedData?,
    val message: String? = null
)

data class MatchesPaginatedData(
    val matches: List<Match>,
    val pagination: PaginationInfo
)

data class MatchDetailResponse(
    val success: Boolean,
    val data: Match?,
    val message: String? = null
)

data class MatchListResponse(
    val success: Boolean,
    val data: List<Match>?,
    val message: String? = null
)

