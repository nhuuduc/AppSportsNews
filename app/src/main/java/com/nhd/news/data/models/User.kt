package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("full_name")
    val fullName: String? = null,
    
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null,
    
    @SerializedName("gender")
    val gender: String? = null, // male, female, other
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("role")
    val role: String = "user", // user, admin, editor
    
    @SerializedName("email_verified")
    val emailVerified: Boolean = false,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)