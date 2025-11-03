package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class Tag(
    @SerializedName("tag_id")
    val tagId: Int,
    
    @SerializedName("tag_name")
    val tagName: String,
    
    @SerializedName("tag_slug")
    val tagSlug: String,
    
    @SerializedName("created_at")
    val createdAt: String
)
