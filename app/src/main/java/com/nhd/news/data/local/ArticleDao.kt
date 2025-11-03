package com.nhd.news.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Article database operations
 */
@Dao
interface ArticleDao {
    
    /**
     * 插入单个文章
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)
    
    /**
     * 插入多个文章
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)
    
    /**
     * 根据ID获取文章
     */
    @Query("SELECT * FROM articles WHERE articleId = :articleId")
    suspend fun getArticleById(articleId: Int): ArticleEntity?
    
    /**
     * 获取所有文章（按发布日期降序）
     */
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC, createdAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>
    
    /**
     * 根据分类ID获取文章
     */
    @Query("SELECT * FROM articles WHERE categoryId = :categoryId ORDER BY publishedAt DESC, createdAt DESC")
    fun getArticlesByCategory(categoryId: Int): Flow<List<ArticleEntity>>
    
    /**
     * 获取特色文章
     */
    @Query("SELECT * FROM articles WHERE isFeatured = 1 ORDER BY publishedAt DESC LIMIT :limit")
    fun getFeaturedArticles(limit: Int = 5): Flow<List<ArticleEntity>>
    
    /**
     * 获取突发新闻
     */
    @Query("SELECT * FROM articles WHERE isBreakingNews = 1 ORDER BY publishedAt DESC LIMIT :limit")
    fun getBreakingNews(limit: Int = 3): Flow<List<ArticleEntity>>
    
    /**
     * 搜索文章
     */
    @Query("""
        SELECT * FROM articles 
        WHERE title LIKE '%' || :query || '%' 
           OR summary LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
        ORDER BY publishedAt DESC
    """)
    fun searchArticles(query: String): Flow<List<ArticleEntity>>
    
    /**
     * 删除所有文章
     */
    @Query("DELETE FROM articles")
    suspend fun deleteAllArticles()
    
    /**
     * 删除过期的缓存（超过7天）
     */
    @Query("DELETE FROM articles WHERE cachedAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)
    
    /**
     * 更新文章的点赞状态
     */
    @Query("UPDATE articles SET isLiked = :isLiked, likeCount = :likeCount WHERE articleId = :articleId")
    suspend fun updateLikeStatus(articleId: Int, isLiked: Boolean, likeCount: Int)
    
    /**
     * 更新文章的浏览次数
     */
    @Query("UPDATE articles SET viewCount = :viewCount WHERE articleId = :articleId")
    suspend fun updateViewCount(articleId: Int, viewCount: Int)
    
    /**
     * 获取缓存的文章数量
     */
    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getCachedArticlesCount(): Int
}

