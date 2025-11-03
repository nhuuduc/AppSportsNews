package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class Match(
    @SerializedName("match_id")
    val matchId: Int,
    
    @SerializedName("home_team_id")
    val homeTeamId: Int,
    
    @SerializedName("away_team_id")
    val awayTeamId: Int,
    
    @SerializedName("category_id")
    val categoryId: Int,
    
    @SerializedName("tournament_name")
    val tournamentName: String? = null,
    
    @SerializedName("match_date")
    val matchDate: String,
    
    @SerializedName("venue")
    val venue: String? = null,
    
    @SerializedName("home_score")
    val homeScore: Int? = null,
    
    @SerializedName("away_score")
    val awayScore: Int? = null,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("highlight_url")
    val highlightUrl: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    // Additional fields for joined data
    @SerializedName("home_team")
    val homeTeam: Team? = null,
    
    @SerializedName("away_team")
    val awayTeam: Team? = null,
    
    @SerializedName("category")
    val category: Category? = null
)
