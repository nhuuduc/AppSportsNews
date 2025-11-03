package com.nhd.news.data.models

import com.google.gson.annotations.SerializedName

data class Article(
    @SerializedName("article_id")
    val articleId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("slug")
    val slug: String,
    
    @SerializedName("summary")
    val summary: String? = null,
    
    @SerializedName("content")
    val content: String? = null,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @SerializedName("category_id")
    val categoryId: Int,
    
    @SerializedName("author_id")
    val authorId: Int,
    
    @SerializedName("view_count")
    val viewCount: Int = 0,
    
    @SerializedName("like_count")
    val likeCount: Int = 0,
    
    @SerializedName("is_liked")
    val isLiked: Boolean = false,
    
    @SerializedName("comment_count")
    val commentCount: Int = 0,
    
    @SerializedName("is_featured")
    val isFeatured: Boolean = false,
    
    @SerializedName("is_breaking_news")
    val isBreakingNews: Boolean = false,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("published_at")
    val publishedAt: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    // Additional fields from JOIN queries
    @SerializedName("category_name")
    val categoryName: String? = null,
    
    @SerializedName("author_name")
    val authorName: String? = null,
    
    // Related data
    @SerializedName("tags")
    val tags: List<Tag>? = null,
    
    @SerializedName("images")
    val images: List<ArticleImage>? = null,
    
    @SerializedName("author")
    val author: User? = null,
    
    @SerializedName("category")
    val category: Category? = null
)
