package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class VideosResponse(
    @SerializedName("error")
    val error: Boolean = false,
    
    @SerializedName("videos")
    val videos: List<Video>,
    
    @SerializedName("total")
    val total: Int = 0,
    
    @SerializedName("page")
    val page: Int = 1,
    
    @SerializedName("page_size")
    val pageSize: Int = 20
)
