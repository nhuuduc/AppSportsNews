package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class ArticleImage(
    @SerializedName("image_id")
    val imageId: Int,
    
    @SerializedName("article_id")
    val articleId: Int,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("caption")
    val caption: String? = null,
    
    @SerializedName("display_order")
    val displayOrder: Int = 0,
    
    @SerializedName("created_at")
    val createdAt: String
)