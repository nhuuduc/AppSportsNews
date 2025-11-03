package com.nhd.news.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Article database operations
 */
@Dao
interface ArticleDao {
    
    /**
     * Thêm một bài viết
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)
    
    /**
     * Thêm nhiều bài viết
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)
    
    /**
     * Lấy bài viết theo ID
     */
    @Query("SELECT * FROM articles WHERE articleId = :articleId")
    suspend fun getArticleById(articleId: Int): ArticleEntity?
    
    /**
     * Lấy tất cả bài viết (sắp xếp theo ngày đăng giảm dần)
     */
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC, createdAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>
    
    /**
     * Lấy bài viết theo category ID
     */
    @Query("SELECT * FROM articles WHERE categoryId = :categoryId ORDER BY publishedAt DESC, createdAt DESC")
    fun getArticlesByCategory(categoryId: Int): Flow<List<ArticleEntity>>
    
    /**
     * Lấy bài viết nổi bật
     */
    @Query("SELECT * FROM articles WHERE isFeatured = 1 ORDER BY publishedAt DESC LIMIT :limit")
    fun getFeaturedArticles(limit: Int = 5): Flow<List<ArticleEntity>>
    
    /**
     * Lấy tin tức khẩn cấp
     */
    @Query("SELECT * FROM articles WHERE isBreakingNews = 1 ORDER BY publishedAt DESC LIMIT :limit")
    fun getBreakingNews(limit: Int = 3): Flow<List<ArticleEntity>>
    
    /**
     * Tìm kiếm bài viết
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
     * Xóa tất cả bài viết
     */
    @Query("DELETE FROM articles")
    suspend fun deleteAllArticles()
    
    /**
     * Xóa cache đã hết hạn (quá 7 ngày)
     */
    @Query("DELETE FROM articles WHERE cachedAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)
    
    /**
     * Cập nhật trạng thái like của bài viết
     */
    @Query("UPDATE articles SET isLiked = :isLiked, likeCount = :likeCount WHERE articleId = :articleId")
    suspend fun updateLikeStatus(articleId: Int, isLiked: Boolean, likeCount: Int)
    
    /**
     * Cập nhật số lượt xem của bài viết
     */
    @Query("UPDATE articles SET viewCount = :viewCount WHERE articleId = :articleId")
    suspend fun updateViewCount(articleId: Int, viewCount: Int)
    
    /**
     * Lấy số lượng bài viết đã cache
     */
    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getCachedArticlesCount(): Int
}

