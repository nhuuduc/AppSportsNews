package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class TrendingArticlesResponse(
    @SerializedName("articles")
    val articles: List<Article>
)
