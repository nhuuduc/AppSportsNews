package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("category_id")
    val categoryId: Int,
    
    @SerializedName("category_name")
    val categoryName: String,
    
    @SerializedName("category_slug")
    val categorySlug: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("icon_url")
    val iconUrl: String? = null,
    
    @SerializedName("parent_id")
    val parentId: Int? = null,
    
    @SerializedName("display_order")
    val displayOrder: Int = 0,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: String
)
