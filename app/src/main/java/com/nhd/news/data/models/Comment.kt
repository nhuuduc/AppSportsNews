package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("comment_id")
    val commentId: Int,
    
    @SerializedName("article_id")
    val articleId: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("parent_comment_id")
    val parentCommentId: Int? = null,
    
    @SerializedName("is_approved")
    val isApproved: Boolean = false,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    // Additional fields from JOIN queries
    @SerializedName("user_name")
    val userName: String? = null,
    
    @SerializedName("user_avatar")
    val userAvatar: String? = null,
    
    // Related data
    @SerializedName("replies")
    val replies: List<Comment>? = null
)