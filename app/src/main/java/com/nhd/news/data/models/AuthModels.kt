package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("device_info")
    val deviceInfo: String? = null
)

// Register Request
data class RegisterRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("full_name")
    val fullName: String? = null
)

// Login Response
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("session_token")
    val sessionToken: String? = null,
    
    @SerializedName("user")
    val user: User? = null
)

// Register Response
data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("username")
    val username: String? = null
)

// Verify Email Response
data class VerifyEmailResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String
)

// Session Verify Response
data class SessionVerifyResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("user")
    val user: User? = null
)

// Generic API Response
data class ApiResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String
)

// Like Response
data class LikeResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("liked")
    val liked: Boolean,
    
    @SerializedName("article_id")
    val articleId: Int? = null,
    
    @SerializedName("like_count")
    val likeCount: Int? = null
)

// Comment (updated with likes)
data class CommentItem(
    @SerializedName("comment_id")
    val commentId: Int,
    
    @SerializedName("article_id")
    val articleId: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("parent_comment_id")
    val parentCommentId: Int? = null,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("is_approved")
    val isApproved: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("author_name")
    val authorName: String,
    
    @SerializedName("author_avatar")
    val authorAvatar: String? = null,
    
    @SerializedName("like_count")
    val likeCount: Int = 0,
    
    @SerializedName("is_liked")
    var isLiked: Boolean = false
)

// Comment Post Response
data class CommentResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("comment")
    val comment: CommentItem? = null
)

// Comment Like Response
data class CommentLikeResponse(
    @SerializedName("comment_id")
    val commentId: Int,
    
    @SerializedName("like_count")
    val likeCount: Int,
    
    @SerializedName("is_liked")
    val isLiked: Boolean,
    
    @SerializedName("message")
    val message: String? = null
)

