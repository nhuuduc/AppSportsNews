package com.nhd.news.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache Manager - Quản lý và xóa cache đã hết hạn
 */
@Singleton
class CacheManager @Inject constructor(
    private val articleDao: ArticleDao
) {
    companion object {
        // Thời gian cache hợp lệ: 7 ngày
        private val CACHE_VALIDITY_DAYS = 7L
    }
    
    /**
     * Xóa cache đã quá 7 ngày
     */
    fun clearOldCache() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val expirationTime = System.currentTimeMillis() - 
                    TimeUnit.DAYS.toMillis(CACHE_VALIDITY_DAYS)
                articleDao.deleteOldCache(expirationTime)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Xóa toàn bộ cache
     */
    fun clearAllCache() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                articleDao.deleteAllArticles()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Lấy số lượng bài viết đã cache
     */
    suspend fun getCachedArticlesCount(): Int {
        return try {
            articleDao.getCachedArticlesCount()
        } catch (e: Exception) {
            0
        }
    }
}

