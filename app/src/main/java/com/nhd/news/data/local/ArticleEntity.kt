package com.nhd.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhd.news.data.models.*

/**
 * Room Entity for caching articles offline
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val articleId: Int,
    val title: String,
    val slug: String,
    val summary: String?,
    val content: String?,
    val thumbnailUrl: String?,
    val categoryId: Int,
    val authorId: Int,
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val commentCount: Int = 0,
    val isFeatured: Boolean = false,
    val isBreakingNews: Boolean = false,
    val status: String?,
    val publishedAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val categoryName: String?,
    val authorName: String?,
    
    // JSON strings for complex objects
    val tagsJson: String?,
    val imagesJson: String?,
    val authorJson: String?,
    val categoryJson: String?,
    
    // Cache metadata
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Convert ArticleEntity to Article model
 */
fun ArticleEntity.toArticle(): Article {
    val gson = Gson()
    
    return Article(
        articleId = articleId,
        title = title,
        slug = slug,
        summary = summary,
        content = content,
        thumbnailUrl = thumbnailUrl,
        categoryId = categoryId,
        authorId = authorId,
        viewCount = viewCount,
        likeCount = likeCount,
        isLiked = isLiked,
        commentCount = commentCount,
        isFeatured = isFeatured,
        isBreakingNews = isBreakingNews,
        status = status,
        publishedAt = publishedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        categoryName = categoryName,
        authorName = authorName,
        tags = tagsJson?.let { 
            gson.fromJson(it, object : TypeToken<List<Tag>>() {}.type)
        },
        images = imagesJson?.let { 
            gson.fromJson(it, object : TypeToken<List<ArticleImage>>() {}.type)
        },
        author = authorJson?.let { 
            gson.fromJson(it, User::class.java)
        },
        category = categoryJson?.let { 
            gson.fromJson(it, Category::class.java)
        }
    )
}

/**
 * Convert Article to ArticleEntity
 */
fun Article.toEntity(): ArticleEntity {
    val gson = Gson()
    
    return ArticleEntity(
        articleId = articleId,
        title = title,
        slug = slug,
        summary = summary,
        content = content,
        thumbnailUrl = thumbnailUrl,
        categoryId = categoryId,
        authorId = authorId,
        viewCount = viewCount,
        likeCount = likeCount,
        isLiked = isLiked,
        commentCount = commentCount,
        isFeatured = isFeatured,
        isBreakingNews = isBreakingNews,
        status = status,
        publishedAt = publishedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        categoryName = categoryName,
        authorName = authorName,
        tagsJson = tags?.let { gson.toJson(it) },
        imagesJson = images?.let { gson.toJson(it) },
        authorJson = author?.let { gson.toJson(it) },
        categoryJson = category?.let { gson.toJson(it) }
    )
}

