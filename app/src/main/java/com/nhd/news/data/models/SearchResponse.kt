package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("error")
    val error: Boolean = false,
    
    @SerializedName("keyword")
    val keyword: String = "",
    
    @SerializedName("type")
    val type: String = "all",
    
    @SerializedName("articles")
    val articles: List<Article> = emptyList(),
    
    @SerializedName("matches")
    val matches: List<Match> = emptyList(),
    
    @SerializedName("teams")
    val teams: List<Team> = emptyList(),
    
    @SerializedName("videos")
    val videos: List<Video> = emptyList(),
    
    @SerializedName("total")
    val total: Int = 0
)

