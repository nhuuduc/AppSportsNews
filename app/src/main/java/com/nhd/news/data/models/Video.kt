package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("video_id")
    val videoId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("video_url")
    val videoUrl: String,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @SerializedName("duration")
    val duration: Int? = null, // in seconds
    
    @SerializedName("category_id")
    val categoryId: Int? = null,
    
    @SerializedName("match_id")
    val matchId: Int? = null,
    
    @SerializedName("view_count")
    val viewCount: Int = 0,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    // Additional fields for joined data
    @SerializedName("category")
    val category: Category? = null,
    
    @SerializedName("match")
    val match: Match? = null
)
