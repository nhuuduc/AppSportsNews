package com.nhd.news.data.api

import com.nhd.news.data.models.Team
import retrofit2.Response
import retrofit2.http.*

interface TeamApiService {
    
    @GET("teams")
    suspend fun getTeams(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<TeamsResponse>
    
    @GET("teams/{id}")
    suspend fun getTeamById(
        @Path("id") teamId: Int
    ): Response<TeamDetailResponse>
}

data class TeamsResponse(
    val success: Boolean,
    val data: TeamsPaginatedData?,
    val message: String? = null
)

data class TeamsPaginatedData(
    val teams: List<Team>,
    val pagination: PaginationInfo
)

data class TeamDetailResponse(
    val success: Boolean,
    val data: Team?,
    val message: String? = null
)
