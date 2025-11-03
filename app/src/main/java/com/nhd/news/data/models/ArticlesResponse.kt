package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class ArticlesResponse(
    @SerializedName("error")
    val error: Boolean = false,
    
    @SerializedName("articles")
    val articles: List<Article>,
    
    @SerializedName("total")
    val total: Int = 0,
    
    @SerializedName("page")
    val page: Int = 1,
    
    @SerializedName("page_size")
    val pageSize: Int = 20
)
