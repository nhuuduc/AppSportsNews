package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class Team(
    @SerializedName("team_id")
    val teamId: Int,
    
    @SerializedName("team_name")
    val teamName: String,
    
    @SerializedName("team_code")
    val teamCode: String? = null,
    
    @SerializedName("logo_url")
    val logoUrl: String? = null,
    
    @SerializedName("category_id")
    val categoryId: Int? = null,
    
    @SerializedName("country")
    val country: String? = null,
    
    @SerializedName("stadium")
    val stadium: String? = null,
    
    @SerializedName("founded_year")
    val foundedYear: Int? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: String
)
